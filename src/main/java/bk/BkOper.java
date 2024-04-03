package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import bk.gen.Account;
import bk.gen.BkConfig;
import bk.gen.Transaction;
import js.app.AppOper;
import js.base.BasePrinter;

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
    todo("more longHelp to come later...");
    super.longHelp(b);
  }

  @Override
  public BkConfig config() {
    if (mConfig == null)
      mConfig = (BkConfig) super.config();
    return mConfig;
  }

  @Override
  public void perform() {

    if (EXP) {

      long todaySeconds = dateToEpochSeconds("");
      pr("today epoch seconds:", todaySeconds);
      var dateStr = epochSecondsToDateString(todaySeconds);
      pr("today:", dateStr);

      var m = map();
      String ss[] = { "", "2024/10/04", "10/04", "/10/4", "2024  10  04", "10/4", "8/4", "024/10/2",
          "24/10/2", "apr 1", "2023 Apr 1", };
      for (var s : ss) {
        var c = DATE_VALIDATOR.validate(s);
        int epochSeconds = c.typedValue();
        String res = "";
        if (epochSeconds != 0)
          res = epochSecondsToDateString(epochSeconds);
        m.putNumbered(s, res);
      }
      pr(m);
      halt();
    }

    storage().read();

    var mgr = winMgr();

    try {
      mgr.open();
      mAccounts = new AccountList(this);
      mTransactions = new TransactionLedger(null, this);
      sAccountsView = mAccounts;
      sTransactionsView = mTransactions;

      
      // Create a root container
      mgr.pushContainer();
      {

        {
          // Construct ledger
          mgr.pct(100);
          mgr.thinBorder();
          mgr.window(mAccounts);
        }
        //        mgr.pct(75);
        //        {
        //          //mgr.horz().pushContainer();
        //          {
        //            // mgr.chars(15).window();
        //            mgr.roundedBorder();
        //            //            if (false)
        //            //              mgr.handler(ourLedger);
        //            // mgr.handler(form);
        //            mgr.window(form);
        //            //            mgr.thinBorder();
        //            //            mgr.pct(20).window();
        //          }
        //          //mgr.popContainer();
        //        }
      }
      mgr.doneConstruction();
      focusManager().push(mAccounts);
      mgr.mainLoop();
    } catch (Throwable t) {
      setError(mgr.closeIfError(t));
    }
  }

  private BkConfig mConfig;
  private AccountList mAccounts;
  private TransactionLedger mTransactions;

  // ------------------------------------------------------------------
  // AccountListListener
  // ------------------------------------------------------------------

  @Override
  public void editAccount(Account account) {
    var form = new AccountForm(AccountForm.TYPE_EDIT, account, this);
    addToMainView(form);
  }

  @Override
  public void addAccount() {
    var form = new AccountForm(AccountForm.TYPE_ADD, null, this);
    addToMainView(form);
  }

  @Override
  public void viewAccount(Account account) {
    var ledger = new TransactionLedger((t) -> t.credit() == account.number() || t.debit() == account.number(),
        this);
    todo("remove filter, or make it internal based on account number");
    ledger.accountNumber(account.number());
    
    focusManager().push(ledger);
  }

  //------------------------------------------------------------------
  // AccountFormListener
  // ------------------------------------------------------------------

  @Override
  public void editedAccount(AccountForm form, Account account) {

    form.remove();

    if (account == null)
      return;
    pr("editedAccount:", INDENT, account);
    mAccounts.rebuild();
    mAccounts.setCurrentRow(account);
    mAccounts.repaint();
    focusManager().pop();
  }

  //------------------------------------------------------------------
  // TransactionListener
  // ------------------------------------------------------------------

  @Override
  public void editTransaction(int forAccount, Transaction t) {
    var form = new TransactionForm(TransactionForm.TYPE_EDIT, t, this, forAccount);
    addToMainView(form);
  }

  @Override
  public void addTransaction(int forAccount) {
    var form = new TransactionForm(TransactionForm.TYPE_ADD, null, this, forAccount);
    addToMainView(form);
  }

  //------------------------------------------------------------------
  // TransactionFormListener
  // ------------------------------------------------------------------

  @Override
  public void editedTransaction(TransactionForm form, Transaction t) {
    form.remove();
    if (t == null)
      return;
    mTransactions.rebuild();
    mTransactions.setCurrentRow(t);
    mTransactions.repaint();
    focusManager().pop();
  }
}
