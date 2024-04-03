package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import bk.gen.Transaction;

public class TransactionForm extends FormWindow {

  public interface Listener {
    /**
     * Called when user has either modified a transaction or cancelled edits.
     * Returns modified transaction, or null if cancel
     */
    void editedTransaction(TransactionForm form, Transaction t);
  }

  public static final int TYPE_ADD = 0, TYPE_EDIT = 1;

  public TransactionForm(int type, Transaction t, Listener listener, int forAccount) {
    todo("account number is switching from blank to zero");
    var b = nullTo(t, Transaction.DEFAULT_INSTANCE).toBuilder();
    mListener = listener;
    mAccountNumber = forAccount;

    mOrig = b.build();
    if (forAccount != 0) {
      if (b.debit() == 0)
        b.debit(forAccount);
      if (b.credit() == 0)
        b.credit(forAccount);
    }

    pr(VERT_SP, "forAccount:",forAccount,"type:", type, "orig:", mOrig, CR, "editing:", b);
    mType = type;
    mSizeExpr = 12;

    var dt = b.date();
    if (dt == 0)
      dt = epochSecondsToday();
    mdate = validator(DATE_VALIDATOR).value(dt).addField("Date");
    mamount = validator(CURRENCY_VALIDATOR).value(b.amount()).addField("Amount");
    mdr = validator(ACCOUNT_VALIDATOR).value(b.debit()).addField("Dr");
    mcr = validator(ACCOUNT_VALIDATOR).value(b.credit()).addField("Cr");
    mdesc = validator(DESCRIPTION_VALIDATOR).value(b.description()).addField("Description");
    addVertSpace(1);
    addButton("Ok", () -> okHandler());
    addButton("Cancel", () -> cancelHandler());
    addVertSpace(1);
    addMessageLine();
  }

  private void removeFormFromScreen() {
    var m = winMgr();
    var c = m.topLevelContainer();
    c.removeChild(this);

    c.setLayoutInvalid();
    focusManager().set(mOldFocus);
  }

  private void okHandler() {
    String problem = "One or more fields is invalid.";
    var tr = Transaction.newBuilder();

    do {
      if (!(mdate.valid() && mamount.valid() && mdr.valid() && mcr.valid() && mdesc.valid()))
        break;
      problem = null;

      tr.timestamp(System.currentTimeMillis());
      tr.date(mdate.validResult());
      tr.amount(mamount.validResult());
      tr.debit(mdr.validResult());
      tr.credit(mcr.validResult());
      tr.description(mdesc.validResult());

      problem = validateTransaction(tr);
      if (problem != null)
        break;

      problem = "Transaction must involve this account";
      if (mAccountNumber != 0) {
        if ((Integer) mdr.validResult() != mAccountNumber && (Integer) mcr.validResult() != mAccountNumber)
          break;
      }
      problem = null;
    } while (false);

    if (problem != null) {
      setMessage(problem);
      return;
    }
    Transaction edited = null;

    if (mType == TYPE_ADD) {
      edited = tr.build();
      storage().addTransaction(edited);
    } else {
      var orig = mOrig;
      tr.timestamp(orig.timestamp());
      edited = tr;
      storage().deleteTransaction(orig.timestamp());
      storage().addTransaction(edited);
    }
    mListener.editedTransaction(this, edited);
  }

  private void cancelHandler() {
    removeFormFromScreen();
  }

  private Listener mListener;
  private int mType;
  private FocusHandler mOldFocus;
  private WidgetWindow mdate, mamount, mdr, mcr, mdesc;
  private int mAccountNumber;
  private Transaction mOrig;
}
