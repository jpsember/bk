package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import bk.gen.Account;
import bk.gen.Database;
import bk.gen.Transaction;
import js.base.BaseObject;
import js.file.Files;

public class YearEnd extends BaseObject {

  private static final boolean DBK = false && alert("debugging backup operation");

  public void close(long closeTimestampSeconds) {

    calculateTimeDateExpressions(closeTimestampSeconds);

    backupExistingDatabase();

    mRetainedEarningsAccount = createRetainedEarningsAccountIfNec();

    closeAccountsToIncomeSummary();

    // Determine opening balances for ASSETS, LIABILITIES, EQUITY accounts
    for (var a : storage().readAllAccounts()) {
      var at = accountClass(a.number());

      if (at == ACCT_ASSET || at == ACCT_LIABILITY || at == ACCT_EQUITY) {
        var tr = storage().readTransactionsForAccount(a.number());
        long bal = 0;
        for (var t : tr) {
          if (t.date() <= mClosingDate) {
            bal += Util.signedAmount(t, a.number());
          }
        }
        if (bal != 0) {
          mOpeningBalances.put(a.number(), bal);
        }
      }
    }

    log("opening balances:", INDENT, mOpeningBalances);

    // Construct list of transactions occurring after closing date, to be added to the new database
    List<Transaction> pushed = arrayList();
    for (var t : storage().readAllTransactions()) {
      // Don't include generated transactions
      if (t.parent() != 0L)
        continue;
      if (t.date() <= mClosingDate)
        continue;
      pushed.add(t);
    }

    // Remove these pushed transactions from the (old year's) database
    for (var t : pushed) {
      storage().deleteTransaction(t);
    }

    var allAcounts = storage().readAllAccounts();

    // Construct a new database
    // ...not done yet...

    // Persist any changes
    storage().flush();

    // Determine filename for previous year's database
    {
      var f = storage().file();
      var s = Files.removeExtension(f.toString()) + "_" + mCloseDateFilenameExpr.substring(0, 4) + "."
          + Files.EXT_JSON;
      var fPrev = new File(s);
      var fPrevRules = Storage.rulesFile(fPrev);

      if (DBK) {
        files().deletePeacefully(fPrev);
        files().deletePeacefully(fPrevRules);
      }

      Files.assertDoesNotExist(fPrev, "previous year database");
      Files.assertDoesNotExist(fPrevRules, "previous year rules");
      files().moveFile(f, fPrev);
      files().copyFile(storage().rulesFile(), fPrevRules);
    }

    // Discard the existing storage object, and replace with an empty one
    var f = storage().file();
    discardStorage();
    files().write(f, Database.newBuilder());
    storage().read(f);

    for (var a : allAcounts) {
      // Don't write income summary
      if (a.number() == ACCT_INCOME_SUMMARY)
        continue;
      a = a.toBuilder().balance(0);
      storage().addOrReplace(a);
    }

    int retEarn = mRetainedEarningsAccount;
    for (var ent : mOpeningBalances.entrySet()) {
      int anum = ent.getKey();
      if (anum == retEarn)
        continue;
      var tr = newTransaction();
      tr.date(mOpeningDate);
      long bal = ent.getValue();
      if (bal > 0) {
        tr.amount(bal);
        tr.debit(anum);
        tr.credit(retEarn);
      } else {
        tr.amount(-bal);
        tr.debit(retEarn);
        tr.credit(anum);
      }
      tr.description("Open");
      log("adding opening trans:", INDENT, tr);
      storage().addOrReplace(tr);
    }

    for (var tr : pushed) {
      tr = tr.toBuilder().children(null);
      log("adding push trans:", INDENT, tr);
      storage().addOrReplace(tr);
    }
    storage().flush();
  }

  private Files files() {
    return Files.S;
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

  /**
   * Make a backup of the database and rule files
   */
  private void backupExistingDatabase() {

    var dbName = Files.basename(storage().file());
    var backupDir = new File("backup_close_" + dbName + "_" + mCloseDateFilenameExpr);

    // Debug mode : If there's already a backup directory, DON'T make a backup; assume the old one is still valid
    if (DBK && backupDir.exists())
      return;

    Files.assertDoesNotExist(backupDir, "create backup directory");
    files().mkdirs(backupDir);

    List<File> src = arrayList();
    src.add(storage().file());
    src.add(storage().rulesFile());
    for (var f : src) {
      var target = new File(backupDir, f.getName());
      files().copyFile(f, target, true);
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
    zeroAccount(account(ACCT_INCOME_SUMMARY), mRetainedEarningsAccount);
  }

  private int createRetainedEarningsAccountIfNec() {
    var num = ACCT_EQUITY;

    var a = account(num);
    if (a == null) {
      a = Account.newBuilder().number(num).name("Retained Earnings");
      storage().addOrReplace(a);
    }
    return a.number();
  }

  /**
   * Add up transactions through closing date, and generate a transaction to
   * close the balance to zero as of that date.
   */
  private void zeroAccount(Account sourceAccount, int targetAccount) {

    // Calculate balance through closing date
    long balanceAsOfCloseDate = 0;
    {
      var trs = storage().readTransactionsForAccount(sourceAccount.number());
      for (var t : trs) {
        if (t.date() <= mClosingDate) {
          if (t.debit() == sourceAccount.number())
            balanceAsOfCloseDate += t.amount();
          else
            balanceAsOfCloseDate -= t.amount();
        }
      }
    }

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
      storage().addOrReplace(tr);
    }
  }

  private Transaction.Builder newTransaction() {
    var tr = Transaction.newBuilder();
    tr.timestamp(mUniqueTransactionTimestamp++);
    return tr;
  }

  private int mRetainedEarningsAccount;
  private long mClosingDate;
  private long mUniqueTransactionTimestamp = System.currentTimeMillis();
  private long mOpeningDate;
  private Map<Integer, Long> mOpeningBalances = hashMap();
  private String mCloseDateFilenameExpr;
}
