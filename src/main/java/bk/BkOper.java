package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;

import bk.gen.Account;
import bk.gen.BkConfig;
import bk.gen.Transaction;
import js.app.AppOper;
import js.app.HelpFormatter;
import js.base.BasePrinter;
import js.file.Files;

public class BkOper extends AppOper
    implements AccountListListener, AccountForm.Listener, TransactionListener, TransactionForm.Listener {

  @Override
  public String userCommand() {
    return "bk";
  }

  @Override
  protected String shortHelp() {
    return "Bookkeeping program";
  }

  @Override
  public BkConfig defaultArgs() {
    return BkConfig.DEFAULT_INSTANCE;
  }

  @Override
  protected void longHelp(BasePrinter b) {
    var hf = new HelpFormatter();
    hf.addItem("[ file <filename> ]", "file containing database (default: books.json)");
    b.pr(hf);
    super.longHelp(b);
  }

  @Override
  public BkConfig config() {
    if (mConfig == null) {
      mConfig = (BkConfig) super.config();
    }
    return mConfig;
  }

  @Override
  public void perform() {
    logger(new Logger(config().logFile()));

    setUtilConfig(config());
    {
      var f = Files.assertNonEmpty(config().database());
      f = Files.addExpectedExtension(f, Files.EXT_JSON);
      if (!f.exists()) {
        if (!config().create())
          setError("Cannot locate database file:", f);
      }
      storage().read(f);
      RuleManager.SHARED_INSTANCE.applyRulesToAllTransactions();
    }

    if (nonEmpty(config().closeAccounts())) {
      try {
        closeAccounts(config().closeAccounts());
      } catch (Throwable t) {
        setError(t);
      }
      return;
    }

    var mgr = winMgr();

    try {
      mgr.open();
      mAccounts = new AccountList(this, this);

      // Construct root container
      mgr.pushContainer();

      // Add a small header
      {
        var h = new MessageWindow();
        Util.sHeader = h;
        h.setMessageAt(MessageWindow.CENTER, "bk 1.0").setMessageAt(MessageWindow.RIGHT, "opt-x: quit");
        mgr.chars(1).window(h);
      }

      {
        // Create a container that has a horizontal layout, with accounts on the left
        mgr.horz().pushContainer();

        {
          mgr.pct(30);
          mgr.thickBorder();
          mgr.window(mAccounts);
        }
        {
          mgr.pct(70);
          var c = new JContainer();
          mgr.pushContainer(c);
          focusManager().setTopLevelContainer(c);
          mgr.popContainer();
        }

        mgr.popContainer();
      }

      // Add a small footer
      {
        var h = new MessageWindow();
        Util.sFooter = h;
        mgr.chars(1).window(h);
      }

      mgr.popContainer();
      mgr.doneConstruction();
      mgr.mainLoop();
    } catch (Throwable t) {
      setError(mgr.closeIfError(t));
    }
  }

  // ------------------------------------------------------------------
  // AccountListListener
  // ------------------------------------------------------------------

  @Override
  public void editAccount(Account account) {
    var form = new AccountForm(AccountForm.TYPE_EDIT, account, this);
    focusManager().pushAppend(form);
  }

  @Override
  public void addAccount() {
    var form = new AccountForm(AccountForm.TYPE_ADD, null, this);
    focusManager().pushAppend(form);
  }

  @Override
  public void deleteAccount(Account account) {
    var u = UndoManager.SHARED_INSTANCE;
    u.begin("Add Account", accountNumberWithNameString(account));
    storage().deleteAccount(account.number());
    u.end();
    changeManager().registerModifiedAccount(account);
    var v = mAccounts;
    v.rebuild();
    v.repaint();
  }

  @Override
  public void viewAccount(Account account) {
    var v = new TransactionLedger(account.number(), this);
    focusManager().pushAppend(v);
  }

  //------------------------------------------------------------------
  // AccountFormListener
  // ------------------------------------------------------------------

  @Override
  public void editedAccount(AccountForm form, Account account) {
    form.remove();
    if (account != null) {
      var v = mAccounts;
      v.rebuild();
      v.setCurrentRow(account);
      v.repaint();
    }
    focusManager().pop();
  }

  //------------------------------------------------------------------
  // TransactionListener
  // ------------------------------------------------------------------

  @Override
  public void editTransaction(int forAccount, Transaction t) {
    var form = new TransactionForm(TransactionForm.TYPE_EDIT, t, this, forAccount);
    focusManager().pushAppend(form);
  }

  @Override
  public void addTransaction(int forAccount) {
    var form = new TransactionForm(TransactionForm.TYPE_ADD, null, this, forAccount);
    focusManager().pushAppend(form);
  }

  @Override
  public void deleteTransaction(Transaction t) {
    var u = UndoManager.SHARED_INSTANCE;
    u.begin("Delete Transaction");
    storage().deleteTransaction(t.timestamp());
    changeManager().registerModifiedTransaction(t);
    u.end();
  }

  //------------------------------------------------------------------
  // TransactionFormListener
  // ------------------------------------------------------------------

  @Override
  public void editedTransaction(TransactionForm form, Transaction t) {
    form.remove();
    focusManager().pop();
  }

  private BkConfig mConfig;
  private AccountList mAccounts;

  private static final boolean DBK = alert("debugging backup operation");

  // ------------------------------------------------------------------
  // Close accounts
  // ------------------------------------------------------------------
  private void closeAccounts(String dateExpr) {

    mClosingTimestamp = System.currentTimeMillis();

    var dbName = Files.basename(storage().file());

    var res = DATE_VALIDATOR.validate(dateExpr);
    mClosingDate = res.typedValue();

    pr("close accounts;", mClosingDate, dateExpr);
    var x = DATE_VALIDATOR.encode(mClosingDate).replace('/', '_');
    pr("encoded:", x);

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

  private void zeroAccount(Account a, int targetAccount) {
    var tr = Transaction.newBuilder();

    tr.timestamp(mClosingTimestamp);
    mClosingTimestamp++;
    tr.date(mClosingDate);

    if (a.balance() > 0) {
      tr.amount(a.balance());
      tr.debit(targetAccount);
      tr.credit(a.number());
    } else {
      tr.amount(-a.balance());
      tr.debit(a.number());
      tr.credit(targetAccount);
    }
    pr("...adding transaction:", tr);
    storage().addOrReplace(tr);
  }

  // ------------------------------------------------------------------
  // For closing accounts (will move to separate class later)
  // ------------------------------------------------------------------

  private long mClosingDate;
  private long mClosingTimestamp;

}
