package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import bk.gen.Account;
import bk.gen.BkConfig;
import js.app.AppOper;
import js.base.BasePrinter;

public class BkOper extends AppOper implements AccountListListener, AccountForm.Listener {

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
      String ss[] = { "2024/10/04", "10/04", "10/4", "8/4", "024/10/2", "24/10/2", };
      for (var s : ss) {
        var c = DATE_VALIDATOR.validate(s);
        pr("validated:", INDENT, s, "=>", c);
      }
      halt();
    }

    storage().read();

    var mgr = winMgr();

    try {
      mgr.open();
      mAccounts = new AccountList(this);
      mTransactions = new TransactionLedger(null);
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
    todo("view account:", account);
    var ledger = new TransactionLedger(
        (t) -> t.credit() == account.number() || t.debit() == account.number());
    switchToView(ledger);
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

    // Restore focus to the AccountList
    focusManager().set(mAccounts);
  }

}
