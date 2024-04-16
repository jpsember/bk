package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import bk.WidgetWindow.HintListener;
import bk.gen.Transaction;

public class TransactionForm extends FormWindow implements HintListener {

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

    var dt = b.date();
    if (dt == 0)
      dt = defaultEpochSeconds();
    mDescHelper = new TransactionDescriptionHelper();
    mDesc = validator(DESCRIPTION_VALIDATOR).value(b.description()).fieldWidth(80).addField("Description")
        .helper(mDescHelper).hintListener(this);
    mDate = validator(DATE_VALIDATOR).value(dt).addField("Date");
    mAmount = validator(CURRENCY_VALIDATOR).value(b.amount()).addField("Amount");

    mDrV = new AccountValidator();
    mCrV = new AccountValidator();

    mDr = validator(mDrV).value(accountNumberWithNameString(b.debit(), ""))
        .fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField("Dr").helper(new AccountIdHelper());
    mCr = validator(mCrV).value(accountNumberWithNameString(b.credit(), ""))
        .fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField("Cr").helper(new AccountIdHelper());

    addButton("Ok", () -> okHandler());

    addVertSpace(1);
    addMessageLine();
    addVertSpace(-100);

    {
      var f = new FooterWindow();
      addChild(f);
      f.setMessageAt(1, " esc:cancel");
    }
  }

  private void okHandler() {
    var tr = Transaction.newBuilder();
    String problem = "This field is invalid.";
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

      problem = checkNameMissingForNew(mDr);
      if (problem != null)
        break;
      problem = checkNameMissingForNew(mCr);
      if (problem != null)
        break;

      tr.timestamp(storage().uniqueTimestamp());
      tr.date(mDate.validResult());
      tr.amount(mAmount.validResult());
      tr.debit(mDr.validResult());
      tr.credit(mCr.validResult());
      tr.description(mDesc.validResult());

      if (tr.debit() == tr.credit()) {
        problem = "The account numbers cannot be the same!";
        focusManager().set(mDr);
        break;
      }
      problem = null;
      // If user specified accounts that don't exist, create them
      createMissingAccounts(tr, mDr.validationResult().extraString(), mCr.validationResult().extraString());

    } while (false);

    if (problem != null) {
      setMessage(problem);
      return;
    }

    var u = UndoManager.SHARED_INSTANCE;

    Transaction edited = null;

    setDefaultEpochSeconds(tr.date());

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
    mListener.editedTransaction(this, edited);
  }

  private String checkNameMissingForNew(WidgetWindow w) {
    var v = w.validationResult();
    int number = v.typedValue();
    if (account(number) == null) {
      if (v.extraString().isEmpty()) {
        focusManager().set(w);
        return "No such account! Specify a name to create a new one, e.g. '" + number + " xyz'";
      }
    }
    return null;
  }

  @Override
  public void hintChanged(String text) {
    if (mType != TYPE_ADD)
      return;
    todo("if user has edited one of the fields themselves, maybe disable the auto fill in");
    var h = mDescHelper;
    var t = h.transactionForDescription(text);
    log("hintChanged, transaction for", quote(text), ":", INDENT, t);
    if (t == null)
      return;
    todo("be more selective about which fields to change");

    // If any of the auto fields has been human edited, don't change any of them
    if (mAmount.isHumanEdited() || mDr.isHumanEdited() || mCr.isHumanEdited())
      return;

    setToSuggestion(mAmount, t.amount());
    setToSuggestion(mDr, t.debit());
    setToSuggestion(mCr, t.credit());
  }

  private void setToSuggestion(WidgetWindow widget, Object value) {
    widget.setContent(widget.validator().encode(value));
    widget.repaint();
  }

  private Listener mListener;
  private int mType;
  private WidgetWindow mDate, mAmount, mDr, mCr, mDesc;
  private AccountValidator mDrV, mCrV;
  private int mAccountNumber;
  private Transaction mOrig;
  private TransactionDescriptionHelper mDescHelper;

}
