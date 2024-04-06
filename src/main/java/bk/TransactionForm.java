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

    mType = type;
    setSizeChars(12);

    var dt = b.date();
    if (dt == 0)
      dt = epochSecondsToday();
    mDate = validator(DATE_VALIDATOR).value(dt).addField("Date");
    mAmount = validator(CURRENCY_VALIDATOR).value(b.amount()).addField("Amount");
    mDr = validator(ACCOUNT_VALIDATOR).value(accountNumberWithNameString(b.debit(),false)).fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField("Dr").helper(new AccountIdHelper());
    mCr = validator(ACCOUNT_VALIDATOR).value(accountNumberWithNameString(b.credit(),false)).fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField("Cr").helper(new AccountIdHelper());
    mDesc = validator(DESCRIPTION_VALIDATOR).value(b.description()).fieldWidth(80).addField("Description");
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
      if (!mDate.valid()) {
        focusManager().set(mDate);
      }
      if (mDate.showAlert() || mAmount.showAlert() || mDr.showAlert() || mCr.showAlert() || mDesc.showAlert()) {
        break;
      }
      problem = null;

      tr.timestamp(System.currentTimeMillis());
      tr.date(mDate.validResult());
      tr.amount(mAmount.validResult());
      tr.debit(mDr.validResult());
      tr.credit(mCr.validResult());
      tr.description(mDesc.validResult());

      problem = validateTransaction(tr);
      if (problem != null) {
        pr("problem:", problem);
        break;
      }

      problem = "Transaction must involve this account";
      if (mAccountNumber != 0) {
        if ((Integer) mDr.validResult() != mAccountNumber && (Integer) mCr.validResult() != mAccountNumber)
          break;
      }
      problem = null;
    } while (false);

    if (problem != null) {
      pr("setMessage:", problem);
      setMessage(problem);
      return;
    }

    Transaction edited = null;

    if (mType == TYPE_ADD) {
      edited = tr.build();
      storage().addTransaction(edited);
      changeManager().registerModifiedTransactions(edited).dispatch();
    } else {
      var orig = mOrig;
      tr.timestamp(orig.timestamp());
      edited = tr;
      undoTransaction(orig);
      applyTransaction(edited);
      storage().deleteTransaction(orig.timestamp());
      storage().addTransaction(edited);
      changeManager().registerModifiedTransactions(orig, edited).dispatch();
    }
    mListener.editedTransaction(this, edited);
  }

  private void cancelHandler() {
    removeFormFromScreen();
  }

  private Listener mListener;
  private int mType;
  private FocusHandler mOldFocus;
  private WidgetWindow mDate, mAmount, mDr, mCr, mDesc;
  private int mAccountNumber;
  private Transaction mOrig;
}
