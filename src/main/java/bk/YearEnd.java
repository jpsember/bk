package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import bk.gen.Account;
import bk.gen.BkConfig;
import bk.gen.Database;
import bk.gen.OpenBalanceInfo;
import bk.gen.ShareCalc;
import bk.gen.Transaction;
import js.base.BaseObject;
import js.data.AbstractData;
import js.file.Files;

public class YearEnd extends BaseObject {

  public YearEnd(BkConfig config) {
    mConfig = config.build();
    if (DBK)
      setVerbose(alert("YearEnd verbosity"));
  }

  public void close(long closeTimestampSeconds) {
    log("close:", closeTimestampSeconds);
    todo("!don't allow user to create a rule that refers to yearend accounts (retained earnings, etc)");
    calculateTimeDateExpressions(closeTimestampSeconds);
    calculateFilenames();

    backupExistingDatabase();

    mRetainedEarningsAccount = createRetainedEarningsAccountIfNec();

    calculateStartShareStuff();

    closeAccountsToIncomeSummary();

    determineOpeningBalances();

    var movedTransactions = getTransactionsToMoveToNextYear();

    removeMovedTransactionsFromPrevYear(movedTransactions);

    // Persist any changes
    storage().flush();

    // Read all the accounts, before we move the database to the previous year's file
    var allAcounts = storage().readAllAccounts();

    moveDatabaseToPrevYear();

    replaceDatabaseWithEmpty();

    copyAccountsToNextYear(allAcounts);

    addOpeningTransactionsToNextYear();

    storeMovedTransactionsInNextYear(movedTransactions);

    storage().flush();
  }

  private void calculateTimeDateExpressions(long closeTimestampSeconds) {
    mClosingDate = closeTimestampSeconds;
    mOpeningDate = closeTimestampSeconds + 24 * 3600;
    var openDateExpr = DATE_VALIDATOR.encode(mOpeningDate);
    var closeDateExpr = DATE_VALIDATOR.encode(mClosingDate);

    log("close accounts; seconds:", mClosingDate, "close:", closeDateExpr, "open:", openDateExpr);
    checkState(!openDateExpr.equals(closeDateExpr), "close sec:", closeTimestampSeconds, "open:",
        mOpeningDate, "expr:", openDateExpr);
    mCloseDateFilenameExpr = closeDateExpr.replace('/', '_');
  }

  private void calculateFilenames() {
    final var df = storage().file();
    final var dir = Files.parent(df);
    final var basename = Files.basename(df);

    mDatabaseFile = df;
    {
      checkNotNull(mCloseDateFilenameExpr);
      mBackupDir = new File(dir, "backup_close_" + basename + "_" + mCloseDateFilenameExpr);
    }

    {
      // Determine filename for previous year's database
      var s = basename + "_" + mCloseDateFilenameExpr.substring(0, 4) + "." + Files.EXT_JSON;
      var fPrev = new File(dir, s);
      mPreviousYearFile = fPrev;

    }
  }

  /**
   * Make a backup of the database and rule files
   */
  private void backupExistingDatabase() {

    var backupDir = mBackupDir;

    if (mConfig.testing()) {
      // Delete the backup directory if it exists
      var s = backupDir.toString();
      var suffix = "_" + mCloseDateFilenameExpr;
      checkArgument(s.endsWith(suffix));

      log("deleting existing backup directory");
      files().deleteDirectory(backupDir, suffix);
    }

    Files.assertDoesNotExist(backupDir, "create backup directory");
    log("creating backup directory:", backupDir);
    files().mkdirs(backupDir);

    List<File> src = arrayList();
    src.add(storage().file());
    for (var f : src) {
      var target = new File(backupDir, f.getName());
      files().copyFile(f, target, true);
    }
  }

  private int createRetainedEarningsAccountIfNec() {
    var num = ACCT_RETAINED_EARNINGS;

    var a = account(num);
    if (a == null) {
      a = Account.newBuilder().number(num).name("Retained Earnings");
      storage().addOrReplace(a);
    }
    return a.number();
  }

  /**
   * Determine share quantity and book values for new year, for all accounts
   * that track such things
   */
  private void calculateStartShareStuff() {
    mShareCalcMap = hashMap();
    for (var a : storage().readAllAccounts()) {
      if (!a.stock())
        continue;

      var sc = new StockCalculator();
      sc.setVerbose(a.number() == 1400);
      sc.withAccountNumber(a.number());
      sc.withTransactions(storage().readTransactionsForAccount(a.number()));
      sc.withClosingDate(mClosingDate);

      var stats = sc.toFiscalYearEnd();
      if (a.number() == 1400)
        checkState(stats.bookValue() != 0, "current year has no book value; but here is the total:", INDENT,
            sc.all(), CR, stats);
      mShareCalcMap.put(a.number(), stats.build());
      //      pr("stored share calc map:", a.number(), INDENT, small(stats));
    }
  }

  /**
   * Transfer balances from revenue and expense accounts to an income summary
   * account, then transfer that account's balance to the equity account
   */
  private void closeAccountsToIncomeSummary() {

    // Create an income summary account

    // Verify that there's not already an income summary account
    var incomeSummaryAccount = account(ACCT_INCOME_SUMMARY);
    log("creating income summary account", ACCT_INCOME_SUMMARY);

    if (incomeSummaryAccount != null)
      badState("Account already exists:", INDENT, incomeSummaryAccount);

    incomeSummaryAccount = Account.newBuilder().name("Income Summary").number(ACCT_INCOME_SUMMARY);
    storage().addOrReplace(incomeSummaryAccount);

    for (var a : storage().readAllAccounts()) {
      if (a.balance() == 0)
        continue;

      var ac = accountClass(a.number());

      // Process revenues
      if (ac == ACCT_INCOME && a.number() != incomeSummaryAccount.number())
        zeroAccount(a, ACCT_INCOME_SUMMARY);

      // Process expenses
      if (ac == ACCT_EXPENSE)
        zeroAccount(a, ACCT_INCOME_SUMMARY);
    }

    // Close income summary account to equity
    log("zeroing INCOME_SUMMARY account to retained earnings account");
    zeroAccount(account(ACCT_INCOME_SUMMARY), mRetainedEarningsAccount);
  }

  /**
   * Add up transactions through closing date, and generate a transaction to
   * close the balance to zero as of that date.
   */
  private void zeroAccount(Account sourceAccount, int targetAccount) {

    log("zeroing account:", small(sourceAccount), "to:", targetAccount);

    // Calculate balance through closing date
    long balanceAsOfCloseDate = 0;
    {
      var trs = storage().readTransactionsForAccount(sourceAccount.number());
      if (DBK) {
        // Sort the transactions by date for debugging sanity
        trs.sort(TRANSACTION_COMPARATOR);
      }
      log("...read transactions for", sourceAccount.number(), "=>", trs.size());
      for (var t : trs) {
        log("..... date:", t.date(), "?<=?", mClosingDate, INDENT, small(t));
        if (t.date() <= mClosingDate) {
          balanceAsOfCloseDate += Util.signedAmount(t, sourceAccount.number());
        }
      }
    }
    log("balance as of close date:", balanceAsOfCloseDate);

    if (balanceAsOfCloseDate != 0) {
      var tr = newTransaction();
      tr.date(mClosingDate);

      if (balanceAsOfCloseDate > 0) {
        tr.amount(balanceAsOfCloseDate);
        tr.debit(targetAccount);
        tr.credit(sourceAccount.number());
      } else {
        tr.amount(-balanceAsOfCloseDate);
        tr.debit(sourceAccount.number());
        tr.credit(targetAccount);
      }
      log("adding closing tr:", small(tr));
      storage().addOrReplace(tr);
    }
  }

  private void determineOpeningBalances() {
    // Determine opening balances for ASSETS, LIABILITIES, EQUITY accounts
    //
    for (var a : storage().readAllAccounts()) {
      var db = a.number() == 1008;
      if (db)
        pr(VERT_SP, "Determine opening balance for:", a.name());

      var at = accountClass(a.number());
      if (db)
        pr("acct class:", at);

      var sc = mShareCalcMap.get(a.number());
      todo("if sc not null, store in comment");
      if (at == ACCT_ASSET || at == ACCT_LIABILITY || at == ACCT_EQUITY) {
        log("determining open balance for", a.number());

        long bal = 0;
        {
          var tr = storage().readTransactionsForAccount(a.number());
          if (DBK)
            tr.sort(TRANSACTION_COMPARATOR);
          for (var t : tr) {
            if (t.date() <= mClosingDate) {
              if (db)
                pr("proc:", small(t));
              bal += Util.signedAmount(t, a.number());
              if (db)
                pr("added", Util.signedAmount(t, a.number()), ", bal now", bal);
            }
          }
        }
        var bi = OpenBalanceInfo.newBuilder();
        bi.balance(bal);
        bi.shareCalc(sc);
        var bj = bi.build();
        mOpeningBalances.put(a.number(), bj);
        if (db)
          pr("stored in map:", INDENT, bj);
        sc = null;
      }
      checkState(sc == null, "unexpected ShareCalc for account:", a.number());
    }
    log("opening balances:", INDENT, mOpeningBalances);
  }

  /**
   * Get a list of the transactions occurring after the closing date, to be
   * moved to the next year's database
   */
  private List<Transaction> getTransactionsToMoveToNextYear() {

    List<Transaction> movedTransactions = arrayList();

    for (var t : storage().readAllTransactions()) {
      // Don't include generated transactions
      if (t.parent() != 0L) {
        todo("What do we do with generated transactions?");
        continue;
      }
      if (t.date() <= mClosingDate)
        continue;
      movedTransactions.add(t);
    }
    return movedTransactions;
  }

  private void removeMovedTransactionsFromPrevYear(List<Transaction> movedTransactions) {
    // Remove these pushed transactions from the (old year's) database
    for (var t : movedTransactions) {
      log("...deleting from old database:", small(t));
      storage().deleteTransaction(t);
    }
  }

  private void moveDatabaseToPrevYear() {
    // Determine filename for previous year's database
    var fPrev = mPreviousYearFile;
    if (mConfig.testing()) {
      files().deletePeacefully(fPrev);
    }
    Files.assertDoesNotExist(fPrev, "previous year database");
    files().moveFile(mDatabaseFile, fPrev);
  }

  // Discard the existing storage object, and replace with an empty one
  private void replaceDatabaseWithEmpty() {
    var f = storage().file();
    discardStorage();
    files().write(f, Database.newBuilder());
    storage().read(f);
  }

  private void copyAccountsToNextYear(List<Account> allAccounts) {
    for (var a : allAccounts) {
      // Don't write income summary
      if (a.number() == ACCT_INCOME_SUMMARY)
        continue;
      a = a.toBuilder().balance(0);
      storage().addOrReplace(a);
    }
  }

  private void addOpeningTransactionsToNextYear() {

    // Add opening transactions to new database
    //
    int retEarn = mRetainedEarningsAccount;
    for (var ent : mOpeningBalances.entrySet()) {
      int anum = ent.getKey();
      if (anum == retEarn)
        continue;
      var ap = account(anum);
      var bi = ent.getValue();

      if (bi.balance() == 0)
        continue;

      var tr = newTransaction();
      tr.date(mOpeningDate);
      long bal = bi.balance();
      if (bal > 0) {
        tr.amount(bal);
        tr.debit(anum);
        tr.credit(retEarn);
      } else {
        tr.amount(-bal);
        tr.debit(retEarn);
        tr.credit(anum);
      }

      var desc = "Open";
      if (ap.stock()) {
        var sc = bi.shareCalc();
        if (sc != null) {
          desc = String.format("=%.3f;%.2f (Open)", sc.shares(), sc.bookValue());
        }
      }
      tr.description(desc);
      log("adding opening trans:", small(tr));
      storage().addOrReplace(tr);
    }
  }

  private void storeMovedTransactionsInNextYear(List<Transaction> movedTransactions) {
    for (var tr : movedTransactions) {
      // Construct a new transaction just to get a new timestamp
      var newTimestamp = newTransaction();
      tr = tr.toBuilder().children(null).timestamp(newTimestamp.timestamp());
      log("adding push trans:", small(tr));
      storage().addOrReplace(tr);
    }
  }

  private static String small(AbstractData d) {
    return d.toJson().toString();
  }

  private Transaction.Builder newTransaction() {
    var tr = Transaction.newBuilder();
    tr.timestamp(mUniqueTransactionTimestamp++);
    return tr;
  }

  private Files files() {
    return Files.S;
  }

  private File mDatabaseFile;
  private File mBackupDir;
  private File mPreviousYearFile;
  private Map<Integer, ShareCalc> mShareCalcMap;
  private BkConfig mConfig;
  private int mRetainedEarningsAccount;
  private long mClosingDate;
  private long mUniqueTransactionTimestamp = System.currentTimeMillis();
  private long mOpeningDate;
  private Map<Integer, OpenBalanceInfo> mOpeningBalances = hashMap();
  private String mCloseDateFilenameExpr;
}
