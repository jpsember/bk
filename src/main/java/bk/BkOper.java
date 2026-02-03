package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;

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
  protected void longHelp(BasePrinter b) {
    var hf = new HelpFormatter();
    hf.addItem("[ database <filename> ]", "file containing database (default: books.json)");
    b.pr(hf);
  }

  @Override
  public BkConfig defaultArgs() {
    return BkConfig.DEFAULT_INSTANCE;
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
    if (devMode()) {
      for (int i = 0; i < 50; i++) pr("<<<devMode>>>");
    }
    processTesting();

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
        // Verify that the closing date is of the form YYYY/MM/DD
        var arg = config().closeAccounts();
        // Replace spaces with '/'
        arg = arg.replace(' ', '/');
        var components = split(arg, '/');
        checkArgument(arg.length() == 10 && components.size() == 3, "expected YYYY/MM/DD; not:", quote(arg));
        var yr = Integer.parseInt(components.get(0));
        checkArgument(yr >= 2020 && yr < 2100, "unexpected year:", yr);
        var res = DATE_VALIDATOR.validate(arg);
        checkArgument(!res.string().isEmpty(), "failed to validate expression:", quote(arg));
        long closingDateSec = res.typedValue();
        var c = new YearEnd(config());
        c.closeBooks(closingDateSec);
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
        h.setMessageAt(MessageWindow.CENTER, "bk 1.0" + (devMode() ? " *** DEV MODE ***" : "")).setMessageAt(MessageWindow.RIGHT, "opt-x: quit");
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

  private void processTesting() {
    if (!config().testing())
      return;
    checkState(!config().create(), "can't create if testing is true");

    var f = Files.assertNonEmpty(config().database());
    f = Files.addExpectedExtension(f, Files.EXT_JSON);

    var database_file = Files.absolute(f);
    var template_file = new File(Files.parent(database_file), "template_" + Files.basename(database_file) + ".json");
    Files.assertExists(template_file, "template file");
    pr("Testing is true; restoring from template:", INDENT, template_file, "=>", CR, database_file);
    files().copyFile(template_file, database_file, true);
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

}
