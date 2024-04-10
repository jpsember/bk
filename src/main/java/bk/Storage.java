package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bk.gen.Account;
import bk.gen.Database;
import bk.gen.Transaction;
import js.base.BaseObject;
import js.data.DataUtil;
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
    Files.S.writeWithPrettyIf(tmp, mDatabase, alert("!writing pretty"));
    Files.S.deleteFile(f);
    Files.S.moveFile(tmp, f);

    mModified = false;
  }

  private void withFile(File file) {
    file = Files.absolute(Files.ifEmpty(file, "database.json"));
    mFile = file;
    log("storage file:", INDENT, Files.infoMap(mFile));
  }

  public File file() {
    return mFile;
  }

  private Map<Integer, Account> accounts() {
    return mDatabase.accounts();
  }

  private Map<Long, Transaction> transactions() {
    return mDatabase.transactions();
  }

  public List<Account> readAllAccounts() {
    return new ArrayList<>(mDatabase.accounts().values());
  }

  public List<Transaction> readAllTransactions() {
    return new ArrayList<>(transactions().values());
  }

  public List<Transaction> readTransactionsForAccount(int accountNumber) {
    checkArgument(accountNumber >= 1000 && accountNumber <= 5999, "unexpected account number:",
        accountNumber);
    List<Transaction> out = arrayList();
    for (var tr : transactions().values()) {
      if (tr.debit() == accountNumber || tr.credit() == accountNumber)
        out.add(tr);
    }
    return out;
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

    // Make a copy of the transactions so we can modify the original map
    List<Transaction> copyOfTrans = arrayList();
    copyOfTrans.addAll(transactions().values());

    for (var t : copyOfTrans) {
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

        if (t.parent() != 0 && transaction(t.parent()) == null) {
          problem = "child has no parent";
          break;
        }

        for (var childId : t.children()) {
          if (transaction(childId) == null) {
            pr("*** Child can't be found; removing all children:", INDENT, t);
            t = t.toBuilder().children(null).build();
            transactions().put(id(t), t);
          }
        }
      } while (false);
      if (problem != null) {
        pr("*** Problem with transaction:", problem, "; deleting", INDENT, t);
        deleteDamaged(t);
      }
    }

    flush();
  }

  private void deleteDamaged(Transaction t) {

    setModified();

    mDatabase.transactions().remove(id(t));

    // If there's a parent, remove its reference to this child
    if (t.parent() != 0) {
      var p = transactions().get(t.parent());
      if (p != null) {
        p = removeChild(p, id(t));
        transactions().put(id(p), p);
      }
    } else {
      // Remove any children
      for (var id : t.children()) {
        transactions().remove(id);
      }
    }

  }

  public Account account(int accountNumber) {
    return mDatabase.accounts().get(accountNumber);
  }

  public Account accountWhichShouldExist(int accountNumber) {
    var x = account(accountNumber);
    if (x == null) {
      alert("<2account was supposed to exist! Number:", accountNumber);
      die("temporary");
    }
    return x;
  }

  public Transaction transaction(long timestamp) {
    return mDatabase.transactions().get(timestamp);
  }

  public Transaction transactionWhichShouldExist(long timestamp) {
    var x = transaction(timestamp);
    if (x == null) {
      alert("<2transaction was supposed to exist! Timestamp:", timestamp);
      die("temporary");
    }
    return x;
  }

  public String accountName(int accountNumber) {
    var account = account(accountNumber);
    if (account == null)
      return "!not found!";
    return account.name();
  }

  /**
   * Probably returns a unique timestamp
   */
  public long uniqueTimestamp() {
    var ts = System.currentTimeMillis();
    ts = Math.max(ts, mUniqueTimestamp + 1);
    mUniqueTimestamp = ts;
    return ts;
  }

  private long mUniqueTimestamp;

  private void adjustBalance(int accountNumber, long currencyAmount) {
    var a = account(accountNumber);
    if (a == null) {
      alert("adjustBalance, no such account number:", accountNumber);
      return;
    }
    a = a.toBuilder().balance(a.balance() + currencyAmount).build();
    accounts().put(a.number(), a);
    setModified();
  }

  private BackupManager bkup() {
    if (mBackups == null) {
      mBackups = new BackupManager(Files.S, Files.parent(file()));
      //mBackups.alertVerbose();
    }
    return mBackups;
  }

  private BackupManager mBackups;
  private Database.Builder mDatabase;
  private File mFile;
  private boolean mModified;

  // ------------------------------------------------------------------
  // Methods that modify the database
  // ------------------------------------------------------------------

  public void addOrReplaceAccount(Account account) {
    account = account.build();
    accounts().put(account.number(), account);
    setModified();
  }

  public static Account mustExist(Account account) {
    if (account == null)
      badState("account is null!");
    return account;
  }

  public void deleteAccount(int number) {
    mustExist(account(number));

    // Delete all transactions
    todo("we probably want a single 'undo' action, not one for each transaction deletion");
    var tr = readTransactionsForAccount(number);
    for (var t : tr)
      deleteTransaction(t);

    accounts().remove(number);
    setModified();
  }

  /**
   * We may need to modify the transaction as we add it, if rules apply.
   * 
   * Return the possibly modified transaction.
   */
  public Transaction addOrReplace(Transaction t) {
    checkState(t.children().length == 0,
        "attempt to add/replace transaction with one that already has children");
    t = t.build();

    var existing = transaction(t.timestamp());

    boolean updtBal = existing == null || existing.debit() != t.debit() || existing.credit() != t.credit()
        || existing.amount() != t.amount();

    if (existing != null) {
      if (updtBal)
        applyTransactionToAccountBalances(existing, true);
      // Delete any child transactions
      for (var childId : existing.children()) {
        deleteTransaction(childId);
      }
    }

    transactions().put(t.timestamp(), t);
    applyTransactionToAccountBalances(t, false);

    setModified();

    RuleManager.SHARED_INSTANCE.applyRules(t);

    return t;
  }

  void replaceTransactionWithoutUpdatingAccountBalances(Transaction t) {
    t = t.build();
    transactions().put(t.timestamp(), t);
    setModified();
  }

  public void deleteTransaction(Transaction t) {
    checkNotNull(t);
    if (!isGenerated(t)) {
      for (var child : getChildTransactions(t))
        deleteTransaction(child);
    }
    var t2 = transactions().remove(t.timestamp());
    checkState(t2 != null, "transaction wasn't in map");
    applyTransactionToAccountBalances(t2, true);
    setModified();
  }

  public List<Transaction> getChildTransactions(Transaction parent) {
    if (parent.children().length == 0)
      return DataUtil.emptyList();
    List<Transaction> trs = arrayList();
    for (var timestamp : parent.children()) {
      var tr = storage().transactionWhichShouldExist(timestamp);
      if (tr == null)
        continue;
      trs.add(tr);
    }
    return trs;
  }

  public void deleteTransaction(long timestamp) {
    var t = transaction(timestamp);
    deleteTransaction(t);
  }

  private void applyTransactionToAccountBalances(Transaction t, boolean negate) {
    checkNotNull(t);
    var amt = t.amount();
    if (negate)
      amt = -amt;
    adjustBalance(t.debit(), amt);
    adjustBalance(t.credit(), -amt);
  }

}
