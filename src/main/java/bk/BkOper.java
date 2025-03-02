package bk;

import static bk.Util.*;
import static js.base.Tools.*;

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

        var res = DATE_VALIDATOR.validate(config().closeAccounts());
        long closingDateSec = res.typedValue();
        //pr("close accounts:",config().closeAccounts(),"res str:",res.string(),"typed val:",res.typedValue());
        var c = new YearEnd();
        c.close(closingDateSec);
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

}
