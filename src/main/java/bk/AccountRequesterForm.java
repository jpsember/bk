package bk;

public class AccountRequesterForm extends FormWindow {

  public interface Listener {
    void processAccount(AccountRequesterForm form, int accountNumber);
  }

  public AccountRequesterForm(String prompt, Listener listener) {
    //alertVerbose();

    //setPreferredSize(new IPoint(40, 8));
    setSizeChars(12);
    mListener = listener;

    addVertSpace(3);
    var val = new AccountValidator();
    mNumber = validator(val).addField(prompt);

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
    mListener.processAccount(this, accNumber);
  }

  private WidgetWindow mNumber;
  private Listener mListener;

}
