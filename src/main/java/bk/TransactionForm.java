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

    boolean newLoc = d84("putting dr/cr first");
    if (newLoc) {
      mDr = validator(new AccountValidator()).value(accountNumberWithNameString(b.debit(), ""))
          .fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField("Dr").helper(new AccountIdHelper());
      mCr = validator(new AccountValidator()).value(accountNumberWithNameString(b.credit(), ""))
          .fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField("Cr").helper(new AccountIdHelper());
      addVertSpace(1);
    }

    var dt = b.date();
    if (dt == 0)
      dt = defaultEpochSeconds();
    mDate = validator(DATE_VALIDATOR).value(dt).addField("Date");
    mDescHelper = new TransactionDescriptionHelper();
    mDesc = validator(new DescriptionValidator()).value(b.description()).fieldWidth(80)
        .addField("Description").helper(mDescHelper).hintListener(this);
    // We need to allow zero-amount transactions for stock switches
    mAmount = validator(new CurrencyValidator().withCanBeZero(true)).value(b.amount()).addField("Amount");


    if (!newLoc) {
      mDr = validator(new AccountValidator()).value(accountNumberWithNameString(b.debit(), ""))
          .fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField("Dr").helper(new AccountIdHelper());
      mCr = validator(new AccountValidator()).value(accountNumberWithNameString(b.credit(), ""))
          .fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField("Cr").helper(new AccountIdHelper());
    }

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
    String problem = "This field is invalid.";
    Transaction.Builder tr = null;

    do {

      if (!isGeneralLedger()) {
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

      tr = newTransactionBuilder();
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

    checkState(tr != null);

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
      todo("This code is duplicated in the TransactionLedger 'move' operation");
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
    if (!accountExists(number)) {
      if (v.extraString().isEmpty()) {
        focusManager().set(w);
        return "No such account! Specify a name to create a new one, e.g. '" + number + " xyz'";
      }
    }
    return null;
  }

  @Override
  public void hintChanged(String text) {
    //alertVerbose();
    if (mType != TYPE_ADD)
      return;
    var h = mDescHelper;
    var t = h.transactionForDescription(text);
    log("hintChanged, transaction for", quote(text), ":", INDENT, t);
    if (t == null)
      return;

    if (!bkConfig().applyHintToTransaction())
      return;

    log("amt he:", mAmount.isHumanEdited());
    log("mDr he:", mDr.isHumanEdited());
    log("mCr he:", mCr.isHumanEdited());

    // If any of the auto fields has been human edited, don't change any of them
    if (mAmount.isHumanEdited() || mDr.isHumanEdited() || mCr.isHumanEdited())
      return;

    // If we're in the general ledger, change all.
    // Otherwise, if one of the debit/credit matches the ledger account number, change all.
    // Else, do nothing (at least one account number must match).
    //
    if (!isGeneralLedger()) {
      if (t.debit() != mAccountNumber && t.credit() != mAccountNumber) {
        if (!alert("neither account matches current account; but ignoring"))
          return;
      }
    }

    setToSuggestion(mAmount, t.amount());
    setToSuggestion(mDr, t.debit());
    setToSuggestion(mCr, t.credit());
  }

  private void setToSuggestion(WidgetWindow widget, Object value) {
    widget.setContent(widget.validator().encode(value));
    widget.repaint();
  }

  private boolean isGeneralLedger() {
    return mAccountNumber == 0;
  }

  private Listener mListener;
  private int mType;
  private WidgetWindow mDate, mAmount, mDr, mCr, mDesc;
  private int mAccountNumber;
  private Transaction mOrig;
  private TransactionDescriptionHelper mDescHelper;

}
