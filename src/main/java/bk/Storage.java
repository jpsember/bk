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
import bk.gen.rules.Rules;
import js.base.BaseObject;
import js.file.Files;
import js.json.JSMap;

public class Storage extends BaseObject {

  public Storage() {
    //alertVerbose();
  }

  public void read(File file) {
    withFile(file);
    var m = JSMap.fromFileIfExists(file());
    if (!file().exists())
      setModified("no database file existed");
    var db = Files.parseAbstractData(Database.DEFAULT_INSTANCE, m);
    mDatabase = db.toBuilder();
    adjustUniqueTimestamp(mDatabase.transactions().values());

    log("read", accounts().size(), "accounts and", transactions().size(), "transactions");
    scanForProblems();
    verifyAccountBalances();

    if (bkConfig().generateTestData())
      generateTestData();

    readRules();
  }

  private void generateTestData() {
    do {
      if (!alert("generating test data"))
        break;
      if (!file().toString().contains("example/database.json")) {
        pr("...database doesn't start with 'example/database.json'");
        break;
      }

      final int MOCK_ACCOUNTS_START = 3900;

      if (true && alert("deleting all mock accounts")) {
        for (var i = MOCK_ACCOUNTS_START; i < 3999; i++) {
          var ac = account(i);
          if (ac == null)
            continue;
          deleteAccount(ac.number());
        }
      }

      int[] acctIds = new int[50];
      if (alert("not adding any"))
        acctIds = new int[0];

      for (int i = 0; i < acctIds.length; i++) {
        var a = Account.newBuilder();
        a.number(MOCK_ACCOUNTS_START + i);
        a.name(randomText(20, false));
        acctIds[i] = a.number();
        if (account(a.number()) != null)
          continue;
        addOrReplace(a);
      }

      var ts2 = System.currentTimeMillis() - 7200000;
      int k = 0;
      for (var aid : acctIds) {
        var ts = ts2 + (k++);
        var tr = readTransactionsForAccount(aid);
        if (!tr.isEmpty())
          continue;

        int ntr = random().nextInt(100) + 8;

        pr("generating", ntr, "transactions for account", aid);
        for (int i = 0; i < ntr; i++) {
          int bid = 0;
          while (true) {
            bid = acctIds[random().nextInt(acctIds.length)];
            if (bid == aid)
              continue;
            break;
          }
          var t = Transaction.newBuilder();
          t.amount(random().nextInt(10000));
          t.date(ts / 1000);
          t.timestamp(ts);
          t.description(randomText(30, false));
          t.debit(aid).credit(aid);
          if (random().nextBoolean()) {
            t.debit(bid);
          } else {
            t.credit(bid);
          }
          addOrReplace(t);
          ts += 3600 * 24 * 1000;
        }
      }

      flush();
    } while (false);
  }

  private void setModified(String reason) {
    if (mModified)
      return;
    log("setting modified; reason:", reason);
    mModified = true;
  }

  /**
   * Dump (most of) the database to the console, with a messages
   */
  public void debug(Object... msg) {
    pr(VERT_SP);
    pr(insertStringToFront(">>>DEBUG:", msg));
    var db = mDatabase.build().toBuilder();
    db.rules(null);
    pr("database:", INDENT, db);
    pr(DASHES, VERT_SP);
  }

  public void flush() {
    if (!mModified)
      return;
    var f = file();
    File tmp = new File(Files.parent(f), "_temporary_.json");
    Files.S.writeWithPrettyIf(tmp, mDatabase, false && alert("!writing pretty"));
    Files.S.deleteFile(f);
    Files.S.moveFile(tmp, f);
    mModified = false;
    log("flushing modified database");
  }

  private void withFile(File file) {
    file = Files.absolute(Files.ifEmpty(file, "database.json"));
    mFile = file;
    if (verbose())
      log("storage file:", INDENT, Files.infoMap(mFile));
  }

  public File file() {
    return mFile;
  }

  private static File rulesFile(File databaseFile) {
    return new File(Files.removeExtension(databaseFile) + ".rules.json");
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
    var x = new ArrayList<>(transactions().values());
    x.sort(TRANSACTION_COMPARATOR2);
    return x;
  }

  public List<Transaction> readTransactionsForAccount(int accountNumber) {
    checkArgument(accountNumber >= 1000 && accountNumber <= 5999, "unexpected account number:",
        accountNumber);
    List<Transaction> out = arrayList();
    for (var tr : transactions().values()) {
      if (tr.debit() == accountNumber || tr.credit() == accountNumber)
        out.add(tr);
    }
    out.sort(TRANSACTION_COMPARATOR2);
    return out;
  }

  private void verifyAccountBalances() {
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
        setModified("account balance was incorrect");
      }
    }
    flush();
  }

  private void scanForProblems() {

    todo("validate the accounts as well");
    {
      Map<Integer, Account> copyOfAcct = hashMap();
      copyOfAcct.putAll(accounts());


      for (var t : copyOfAcct.entrySet()) {
        String problem = null;
        var a = t.getValue();

        do {
          var sc = a.shortcut();
          if (sc.length() > 1) {
            problem = "shortcut is too long";
            break;
          }
        } while (false);
        if (problem != null) {
          pr("*** Problem with account:", problem);
        }
      }
    }

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

    setModified("delete damaged transaction");

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

  public Transaction transaction(long timestamp) {
    return mDatabase.transactions().get(timestamp);
  }

  public String accountName(int accountNumber) {
    var account = account(accountNumber);
    if (account == null)
      return "!not found!";
    return account.name();
  }

  private Database.Builder mDatabase;
  private File mFile;
  private boolean mModified;

  // ------------------------------------------------------------------
  // Methods that modify the database
  // ------------------------------------------------------------------

  public void addOrReplace(Account account) {
    var u = UndoManager.SHARED_INSTANCE;
    account = account.build();
    var existing = accounts().get(account.number());
    if (existing != null)
      u.deleteAccount(existing);
    u.addAccount(account);
    accounts().put(account.number(), account);
    setModified("add or replace account");
  }

  public void deleteAccount(int number) {
    var acc = account(number);
    checkNotNull(acc);

    var u = UndoManager.SHARED_INSTANCE;

    if (u.live()) {
      // Delete all transactions
      var tr = readTransactionsForAccount(number);
      for (var t : tr) {
        u.deleteTransaction(t);
        deleteTransaction(t);
      }
      u.deleteAccount(acc);
    }
    accounts().remove(number);
    setModified("delete account");
  }

  /**
   * Add a transaction. Throws exception if one already exists with that
   * timestamp
   */
  public void add(Transaction t) {
    var existing = transaction(t.timestamp());
    if (existing != null) {
      badState("Attempt to add transaction with same timestamp as another:", t.timestamp(), INDENT, t,
          OUTDENT, "existing:", INDENT, existing);
    }
    addOrReplace(t);
  }

  /**
   * We may need to modify the transaction as we add it, if rules apply.
   * 
   * Return the possibly modified transaction.
   */
  public Transaction addOrReplace(Transaction t) {
    var u = UndoManager.SHARED_INSTANCE;
    if (u.live()) {
      checkState(t.children().length == 0,
          "attempt to add/replace transaction with one that already has children");
    }
    t = t.build();

    var existing = transaction(t.timestamp());
    if (existing != null) {
      u.deleteTransaction(existing);
      applyTransactionToAccountBalances(existing, true);
      if (u.live()) {
        // Delete any child transactions
        for (var childId : existing.children()) {
          deleteTransaction(childId);
        }
      }
    }

    u.addTransaction(t);

    transactions().put(t.timestamp(), t);
    applyTransactionToAccountBalances(t, false);

    setModified("add or replace trans");

    if (u.live()) {
      RuleManager.SHARED_INSTANCE.applyRules(t);
    }

    return t;
  }

  public void replaceTransactionWithoutUpdatingAccountBalances(Transaction t) {
    var u = UndoManager.SHARED_INSTANCE;
    t = t.build();
    var existing = transaction(t.timestamp());
    if (existing != null) {
      u.deleteTransaction(existing);
    }
    u.addTransaction(t);
    transactions().put(t.timestamp(), t);
    setModified("replace transaction w/o update balances");
  }

  public void deleteTransaction(Transaction t) {
    checkNotNull(t);
    var u = UndoManager.SHARED_INSTANCE;
    if (u.live()) {
      if (!isGenerated(t)) {
        for (var child : getChildTransactions(t)) {
          deleteTransaction(child);
        }
      }
    }
    var t2 = transactions().remove(t.timestamp());
    checkState(t2 != null, "transaction wasn't in map");
    u.deleteTransaction(t2);
    applyTransactionToAccountBalances(t2, true);
    setModified("delete transaction");
  }

  public void deleteTransaction(long timestamp) {
    var t = transaction(timestamp);

    if (t == null) {
      pr(VERT_SP, "Can't find transaction with timestamp:", timestamp, "!!!");

      pr("transactions are:");
      for (var ent : transactions().entrySet())
        pr("key:", ent.getKey(), "value:", INDENT, ent.getValue());
      die("bye");
    }

    deleteTransaction(t);
  }

  /**
   * This has no effect if undo or redo is occurring
   */
  private void applyTransactionToAccountBalances(Transaction t, boolean negate) {
    checkNotNull(t);
    if (!UndoManager.SHARED_INSTANCE.live())
      return;
    var amt = t.amount();
    if (negate)
      amt = -amt;
    adjustBalance(t.debit(), amt);
    adjustBalance(t.credit(), -amt);
  }

  private void adjustBalance(int accountNumber, long currencyAmount) {
    if (currencyAmount == 0)
      return;
    var u = UndoManager.SHARED_INSTANCE;
    var a = account(accountNumber);
    checkNotNull(a, "adjustBalance, no such account:", accountNumber);
    u.deleteAccount(a);

    a = a.toBuilder().balance(a.balance() + currencyAmount).build();
    accounts().put(a.number(), a);
    u.addAccount(a);
    setModified("adjust balance");
  }

  private void readRules() {
    log("readRules");

    // Determine if an external rules file should be read instead of the rules field in the database

    // 1) if a rules file exists, and
    // 2) either the database has no rules, or the database file is older than the rules file

    var extRulesFile = rulesFile(file());
    boolean updateFromExternal = extRulesFile.exists()
        && (mDatabase.rules().rules().isEmpty() || extRulesFile.lastModified() > file().lastModified());
    log("external file:", Files.infoMap(extRulesFile));

    Rules r = mDatabase.rules();
    if (updateFromExternal) {
      r = Files.parseAbstractDataOpt(Rules.DEFAULT_INSTANCE, extRulesFile);
      log("parsed external rules:", INDENT, r);
    }

    r = RuleManager.updateRules(r);
    r = RuleManager.parseDates(r);

    log("after update rules, parse:", INDENT, r);
    if (!r.equals(mDatabase.rules())) {
      mDatabase.rules(r);
      setModified("rules have changed");
    }

    RuleManager.SHARED_INSTANCE.setRules(r);
  }

}
