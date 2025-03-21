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
  }

  public void closeBooks(long closeTimestampSeconds) {
    log("closeBooks:", closeTimestampSeconds);

    calculateTimeDateExpressions(closeTimestampSeconds);
    calculateFilenames();
    backupExistingDatabase();
    createRetainedEarningsAccountIfNec();
    calculateStartShareStuff();

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
    storage().debug("copied accounts to next year");
    addOpeningTransactionsToNextYear();
    storage().debug("added opening trans");
    storeMovedTransactionsInNextYear(movedTransactions);
    storage().debug("stored moved transactions");
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

  private void createRetainedEarningsAccountIfNec() {
    var num = ACCT_RETAINED_EARNINGS;

    var a = account(num);
    if (a == null) {
      a = Account.newBuilder().number(num).name("Retained Earnings");
      storage().addOrReplace(a);
    }
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
    }
  }

  private void determineOpeningBalances() {
    // Determine opening balances for ASSETS, LIABILITIES, EQUITY accounts
    //
    for (var a : storage().readAllAccounts()) {

      var at = accountClass(a.number());

      var sc = mShareCalcMap.get(a.number());
      if (at == ACCT_ASSET || at == ACCT_LIABILITY || at == ACCT_EQUITY) {
        log("determining open balance for", a.number());

        long bal = 0;
        {
          var tr = storage().readTransactionsForAccount(a.number());
          for (var t : tr) {
            if (t.date() <= mClosingDate) {
              bal += Util.signedAmount(t, a.number());
            }
          }
        }
        var bi = OpenBalanceInfo.newBuilder();
        bi.balance(bal);
        bi.shareCalc(sc);
        var bj = bi.build();
        mOpeningBalances.put(a.number(), bj);
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
      a = a.toBuilder().balance(0);
      storage().addOrReplace(a);
    }
  }

  private void addOpeningTransactionsToNextYear() {

    // Add opening transactions to new database
    //
    int retainedEearningsNumber = ACCT_RETAINED_EARNINGS;
    for (var ent : mOpeningBalances.entrySet()) {
      int accountNumber = ent.getKey();
      var bi = ent.getValue();
      if (accountNumber == retainedEearningsNumber)
        continue;

      if (bi.balance() == 0)
        continue;

      var ap = account(accountNumber);

      var tr = newTransactionBuilder();
      tr.date(mOpeningDate);
      long bal = bi.balance();
      if (bal > 0) {
        tr.amount(bal);
        tr.debit(accountNumber);
        tr.credit(retainedEearningsNumber);
      } else {
        tr.amount(-bal);
        tr.debit(retainedEearningsNumber);
        tr.credit(accountNumber);
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
      storage().add(tr);
    }
  }

  private void storeMovedTransactionsInNextYear(List<Transaction> movedTransactions) {
    pr(VERT_SP, "storeMovedTransactions:", INDENT, movedTransactions);
    for (var tr : movedTransactions) {
      tr = tr.toBuilder().children(null).timestamp(uniqueTimestamp());
      log("adding push trans:", small(tr));
      storage().add(tr);
    }
  }

  private static String small(AbstractData d) {
    return d.toJson().toString();
  }

  private Files files() {
    return Files.S;
  }

  private File mDatabaseFile;
  private File mBackupDir;
  private File mPreviousYearFile;
  private Map<Integer, ShareCalc> mShareCalcMap;
  private BkConfig mConfig;
  private long mClosingDate;
  private long mOpeningDate;
  private Map<Integer, OpenBalanceInfo> mOpeningBalances = hashMap();
  private String mCloseDateFilenameExpr;
}
