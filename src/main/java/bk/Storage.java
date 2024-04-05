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
import js.file.BackupManager;
import js.file.Files;
import js.json.JSMap;

public class Storage extends BaseObject {

  public void read(File file) {
    //alertVerbose();
    withFile(file);
    var m = JSMap.fromFileIfExists(file());
    var db = Files.parseAbstractData(Database.DEFAULT_INSTANCE, m);
    mDatabase = db.toBuilder();
    log("read", accounts().size(), "accounts and", transactions().size(), "transactions");
    scanForProblems();
    verifyAccountBalances();
  }

  public void setModified() {
    mModified = true;
  }

  public void flush() {
    if (!mModified)
      return;

    var f = file();
    if (f.exists()) {
      bkup().makeBackup(f);
    }

    File tmp = new File(Files.parent(f), "_temporary_.json");
    Files.S.write(tmp, mDatabase);
    Files.S.deleteFile(f);
    Files.S.moveFile(tmp, f);

    mModified = false;
  }

  private void withFile(File file) {
    file = Files.absolute(Files.ifEmpty(file, "database.json"));
    mFile = file;
    log("storage file:", INDENT, Files.infoMap(mFile));
  }

  private File file() {
    return mFile;
  }

  public Map<Integer, Account> accounts() {
    return mDatabase.accounts();
  }

  public Map<Long, Transaction> transactions() {
    return mDatabase.transactions();
  }

  public void verifyAccountBalances() {
    Map<Integer, Account.Builder> map = hashMap();
    for (var a : accounts().values()) {
      map.put(a.number(), a.toBuilder().balance(0));
    }

    List<Long> deleteTransList = arrayList();
    for (var t : transactions().values()) {
      {
        var dAccount = map.get(t.debit());
        var cAccount = map.get(t.credit());
        if (dAccount == null || cAccount == null) {
          pr("*** transaction references missing account(s):", INDENT, t);
          deleteTransList.add(t.timestamp());
          continue;
        }
        dAccount.balance(dAccount.balance() + t.amount());
        cAccount.balance(cAccount.balance() - t.amount());
        //pr("credit balance for:",cAccount.number(),"sub:",formatCurrency(t.amount()),"now:",formatCurrency(cAccount.balance()),"desc:",t.description());
      }
    }

    for (var tnum : deleteTransList) {
      deleteTransaction(tnum);
    }

    for (var a : map.values()) {
      var orig = accounts().get(a.number());
      if (a.balance() != orig.balance()) {
        pr("*** account", a.number(), ":", a.name(), "balance was incorrect:", INDENT,
            formatCurrency(orig.balance()), ", should be:", CR, formatCurrency(a.balance()));
        accounts().put(a.number(), a.build());
        setModified();
      }
    }
    flush();
  }

  public void scanForProblems() {

    List<Transaction> moveList = arrayList();
    for (var t : transactions().values()) {
      String problem = null;
      outer: do {
        for (int i = 0; i < 2; i++) {
          int an = accountNumber(t, i);
          if (account(an) == null) {
            problem = "missing account";
            break outer;
          }
        }
        if (t.debit() == t.credit()) {
          problem = "account numbers are the same";
          break;
        }
        if (t.timestamp() == 0) {
          problem = "missing timestamp";
          break;
        }
        if (!validDate(t.date())) {
          problem = "invalid date";
          break;
        }
      } while (false);
      if (problem != null) {
        pr("*** Problem with transaction, moved to damaged section:", INDENT, t);
        moveList.add(t);
      }
    }
    for (var t : moveList) {
      mDatabase.damagedTransactions().put(id(t), t);
      mDatabase.transactions().remove(id(t));
      setModified();
    }
    flush();
  }

  public Account account(int accountNumber) {
    return mDatabase.accounts().get(accountNumber);
  }

  public Transaction transaction(long timestamp) {
    return mDatabase.transactions().get(timestamp);
  }

  public String accountName(int accountNumber) {
    var account = account(accountNumber);
    if (account == null)
      return "!not found!";
    return account.name();
  }

  public void addAccount(Account account) {
    account = account.build();
    var existing = account(account.number());
    if (existing != null)
      badState("account already exists!", INDENT, existing);
    accounts().put(account.number(), account);
    setModified();
  }

  public void deleteAccount(int number) {
    accounts().remove(number);
    setModified();
  }

  public void addTransaction(Transaction t) {
    t = t.build();
    var existing = transaction(t.timestamp());
    if (existing != null)
      badState("transaction already exists!", INDENT, existing);
    transactions().put(t.timestamp(), t);
    // Apply transaction to account balances
    {
      // Undo the effect of the transaction on account balances
      adjustBalance(t.debit(), t.amount());
      adjustBalance(t.credit(), -t.amount());
    }
    setModified();
  }

  public void deleteTransaction(long timestamp) {
    var t = transactions().remove(timestamp);
    if (t == null) {
      alert("deleteTransaction, not found; timestamp:", timestamp);
    } else {
      // Undo the effect of the transaction on account balances
      adjustBalance(t.debit(), -t.amount());
      adjustBalance(t.credit(), t.amount());
    }
    setModified();
  }

  private void adjustBalance(int accountNumber, long currencyAmount) {
    var a = account(accountNumber);
    if (a == null) {
      alert("adjustBalance, no such account number:", accountNumber);
      return;
    }
    a = a.toBuilder().balance(a.balance() + currencyAmount).build();
    accounts().put(a.number(), a);
  }

  private BackupManager bkup() {
    if (mBackups == null) {
      mBackups = new BackupManager(Files.S, Files.parent(file()));
      mBackups.alertVerbose();
    }
    return mBackups;
  }

  private BackupManager mBackups;
  private Database.Builder mDatabase;
  private File mFile;
  private boolean mModified;

}
