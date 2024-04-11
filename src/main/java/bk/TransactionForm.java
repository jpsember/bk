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

    mType = type;
    setSizeChars(12);

    var dt = b.date();
    if (dt == 0)
      dt = epochSecondsToday();
    mDate = validator(DATE_VALIDATOR).value(dt).addField("Date");
    mAmount = validator(CURRENCY_VALIDATOR).value(b.amount()).addField("Amount");
    mDr = validator(ACCOUNT_VALIDATOR).value(accountNumberWithNameString(b.debit(), false))
        .fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField("Dr").helper(new AccountIdHelper());
    mCr = validator(ACCOUNT_VALIDATOR).value(accountNumberWithNameString(b.credit(), false))
        .fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField("Cr").helper(new AccountIdHelper());
    mDesc = validator(DESCRIPTION_VALIDATOR).value(b.description()).fieldWidth(80).addField("Description");
    addButton("Ok", () -> okHandler());
    addButton("Cancel", () -> cancelHandler());
    addVertSpace(1);
    addMessageLine();
  }

  private void okHandler() {

    String problem = "One or more fields is invalid.";
    var tr = Transaction.newBuilder();

    do {

      if (mAccountNumber != 0) {
        // If we are editing it for a particular account, and
        // If exactly one of the debit or credit are empty, set them to that account

        // They can't BOTH be empty
        if (!(mDr.isEmpty() && mCr.isEmpty())) {
          if (mDr.isEmpty())
            mDr.value(mAccountNumber).repaint();
          if (mCr.isEmpty())
            mCr.value(mAccountNumber).repaint();
        }
      }

      if (mDate.showAlert() //
          || mAmount.showAlert() //
          || mDr.showAlert() //
          || mCr.showAlert() //
          || mDesc.showAlert()) {
        break;
      }
      problem = null;

      tr.timestamp(storage().uniqueTimestamp());
      tr.date(mDate.validResult());
      tr.amount(mAmount.validResult());
      tr.debit(mDr.validResult());
      tr.credit(mCr.validResult());
      tr.description(mDesc.validResult());

      problem = validateTransaction(tr);
      if (problem != null)
        break;

      problem = "Transaction must involve this account";
      if (mAccountNumber != 0) {
        if ((Integer) mDr.validResult() != mAccountNumber && (Integer) mCr.validResult() != mAccountNumber)
          break;
      }
      problem = null;
    } while (false);

    if (problem != null) {
      setMessage(problem);
      return;
    }

    var u = UndoManager.SHARED_INSTANCE;

    Transaction edited = null;

    if (mType == TYPE_ADD) {
      u.begin("Add Transaction");
      edited = tr.build();
      edited = storage().addOrReplace(edited);
      changeManager().registerModifiedTransactions(edited);
      u.end();
    } else {
      u.begin("Edit Transaction");
      var orig = mOrig;
      tr.timestamp(orig.timestamp());
      edited = tr;
      storage().deleteTransaction(orig.timestamp());
      edited = storage().addOrReplace(edited);
      changeManager().registerModifiedTransactions(orig, edited);
      u.end();
    }
    mark("dispatch should only be called by the window manager?");
    //changeManager().dispatch();
    mListener.editedTransaction(this, edited);
  }

  private void cancelHandler() {
    mListener.editedTransaction(this, null);
  }

  private Listener mListener;
  private int mType;
  private WidgetWindow mDate, mAmount, mDr, mCr, mDesc;
  private int mAccountNumber;
  private Transaction mOrig;
}
