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

  private static final boolean DBK = alert("debugging backup operation");

  public void close(long closeTimestampSeconds) {
    alertVerbose();

    mClosingDate = closeTimestampSeconds;
    mClosingTimestamp = System.currentTimeMillis();

    var dbName = Files.basename(storage().file());
    var closeDateExpr = DATE_VALIDATOR.encode(mClosingDate);
    log("close accounts;", mClosingDate, "expr:", closeDateExpr);
    var x = closeDateExpr.replace('/', '_');
    log("encoded:", x);

    // Verify that there's not already an income summary account
    {
      var zincSumAcct = account(ACCT_INCOME_SUMMARY);
      if (zincSumAcct != null)
        badState("Account already exists:", INDENT, zincSumAcct);
    }

    // Make a backup of the database and rules

    var backupDir = new File("backup_close_" + dbName + "_" + x);

    boolean makeBackup = true;

    // Debug mode : If there's already a backup directory, DON'T make a backup; assume the old one is still valid
    if (DBK && backupDir.exists()) {
      makeBackup = false;
    }

    if (makeBackup) {
      if (DBK)
        files().deleteDirectory(backupDir, "backup_close");

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

    // Create an income summary account

    {
      var incomeSummaryAccount = Account.newBuilder().name("Income Summary").number(ACCT_INCOME_SUMMARY);
      storage().addOrReplace(incomeSummaryAccount);
      for (var a : storage().readAllAccounts()) {
        if (a.balance() == 0)
          continue;

        // Process revenues
        if (a.number() >= ACCT_INCOME && a.number() < ACCT_INCOME + 1000
            && a.number() != incomeSummaryAccount.number()) {
          zeroAccount(a, ACCT_INCOME_SUMMARY);
        }

        // Process expenses
        if (a.number() >= ACCT_EXPENSE && a.number() < ACCT_EXPENSE + 1000) {
          zeroAccount(a, ACCT_INCOME_SUMMARY);
        }

      }
    }

    // Close income summary account to equity
    {
      zeroAccount(account(ACCT_INCOME_SUMMARY), ACCT_EQUITY);
    }

    // Determine opening balances for ASSETS, LIABILITIES, EQUITY accounts
    for (var a : storage().readAllAccounts()) {
      var at = a.number() - (a.number() % 1000);

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
      var s = Files.removeExtension(f.toString()) + "_" + x.substring(0, 4) + "." + Files.EXT_JSON;
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

    todo("write accounts, overflow trans");
  }

  private Files files() {
    return Files.S;
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
      // mOpeningBalances.put(sourceAccount.number(), balanceAsOfCloseDate);
      var tr = Transaction.newBuilder();
      tr.timestamp(mClosingTimestamp);
      mClosingTimestamp++;
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
      pr("...adding transaction:", tr);
      storage().addOrReplace(tr);
    }
  }

  private long mClosingDate;
  private long mClosingTimestamp;
  private Map<Integer, Long> mOpeningBalances = hashMap();

}
