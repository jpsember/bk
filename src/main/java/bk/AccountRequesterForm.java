package bk;

import static bk.Util.*;
import static js.base.Tools.*;

public class AccountRequesterForm extends FormWindow {

  public interface Listener {
    void processAccount(AccountRequesterForm form, int accountNumber);
  }

  public AccountRequesterForm(String prompt, Listener listener) {
    mPrompt = prompt;
    mListener = listener;
  }

  public AccountRequesterForm prepare() {
    checkState(!mPrepared);

    setSizeChars(12);

    addVertSpace(3);

    mNumber = validator(new AccountValidator()).fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField(mPrompt)
        .helper(new AccountIdHelper());
    mAddSummaryTransaction = validator(new YesNoValidator()).value(true).addField("Gen summary tr");
    addButton("Ok", () -> okHandler());
    addVertSpace(1);
    addMessageLine();
    mPrepared = true;
    return this;
  }

  private boolean mPrepared;

  private void okHandler() {

    String problem = "This field is invalid.";

    int accNumber = 0;
    do {
      if (mNumber.showAlert())
        break;
      accNumber = mNumber.validResult();
      problem = null;
    } while (false);

    if (problem != null) {
      setMessage(problem);
      return;
    }
    // If user specified an account that doesn't exist, create it
    createMissingAccount(accNumber, mNumber.validationResult().extraString());
    mListener.processAccount(this, accNumber);
  }

  public boolean addSummaryTransaction() {
    return mAddSummaryTransaction.validResult();
  }

  private WidgetWindow mNumber;
  private WidgetWindow mAddSummaryTransaction;
  private Listener mListener;
  private String mPrompt;

}
