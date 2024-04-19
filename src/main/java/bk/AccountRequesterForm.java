package bk;

import static bk.Util.*;

public class AccountRequesterForm extends FormWindow {

  public interface Listener {
    void processAccount(AccountRequesterForm form, int accountNumber);
  }

  public AccountRequesterForm(String prompt, Listener listener) {
    setSizeChars(12);
    mListener = listener;

    addVertSpace(3);

    mNumber = validator(new AccountValidator()).fieldWidth(CHARS_ACCOUNT_NUMBER_AND_NAME).addField(prompt)
        .helper(new AccountIdHelper());

    addButton("Ok", () -> okHandler());
    addVertSpace(1);
    addMessageLine();
  }

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

  private WidgetWindow mNumber;
  private Listener mListener;

}
