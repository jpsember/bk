package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;

import bk.gen.Account;
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

    // Persist any changes
    storage().flush();
  }

  private Files files() {
    return Files.S;
  }

  private void zeroAccount(Account a, int targetAccount) {
    // Calculate balance through closing date
    long balanceAsOfCloseDate = 0;
    {
      var trs = storage().readTransactionsForAccount(a.number());
      for (var t : trs) {
        if (t.date() <= mClosingDate) {
          if (t.debit() == a.number())
            balanceAsOfCloseDate += t.amount();
          else
            balanceAsOfCloseDate -= t.amount();
        }
      }
    }
    if (balanceAsOfCloseDate == 0)
      return;

    var tr = Transaction.newBuilder();

    tr.timestamp(mClosingTimestamp);
    mClosingTimestamp++;
    tr.date(mClosingDate);

    if (balanceAsOfCloseDate > 0) {
      tr.amount(balanceAsOfCloseDate);
      tr.debit(targetAccount);
      tr.credit(a.number());
    } else {
      tr.amount(-balanceAsOfCloseDate);
      tr.debit(a.number());
      tr.credit(targetAccount);
    }
    pr("...adding transaction:", tr);
    storage().addOrReplace(tr);
  }

  private long mClosingDate;
  private long mClosingTimestamp;

}
