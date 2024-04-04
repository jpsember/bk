package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import bk.gen.Account;

public class AccountForm extends FormWindow {

  public interface Listener {
    /**
     * Called when user has either modified an account or cancelled edits.
     * Returns modified account, or null if cancel
     */
    void editedAccount(AccountForm form, Account account);
  }

  public static final int TYPE_ADD = 0, TYPE_EDIT = 1;

  public AccountForm(int type, Account account, Listener listener) {
    loadTools();
    account = nullTo(account, Account.DEFAULT_INSTANCE).build();
    mOriginalAccount = account;
    mType = type;
    setSizeChars(12);
    mListener = listener;
    mNumber = validator(ACCOUNT_VALIDATOR).value(account.number()).addField("#");
    mName = validator(ACCOUNT_NAME_VALIDATOR).value(account.name()).fieldWidth(ACCOUNT_NAME_MAX_LENGTH)
        .addField("Name");
    addVertSpace(1);
    addButton("Ok", () -> okHandler());
    addButton("Cancel", () -> cancelHandler());
    addVertSpace(1);
    addMessageLine();
  }

  private void okHandler() {
    String problem = "One or more fields is invalid.";
    var ac = Account.newBuilder();
    if (mNumber.valid() && mName.valid()) {
      ac.number(mNumber.validResult());
      ac.name(mName.validResult());
      problem = validateAccount(ac);
    }

    if (problem == null) {

      switch (mType) {
      case TYPE_ADD: {
        var existing = account(ac.number());
        if (existing != null)
          problem = "That account number is taken!";
      }
        break;

      case TYPE_EDIT: {
        var existing = storage().account(ac.number());
        if (existing != null && ac.number() != mOriginalAccount.number())
          problem = "That account number is taken!";
        todo("check for duplicate account names");
      }
        break;
      }
    }
    if (problem != null) {
      setMessage(problem);
      return;
    }

    Account editedAccount = null;

    if (mType == TYPE_ADD) {
      editedAccount = ac;
      storage().addAccount(ac);
    } else {
      // modify original account to include the edits
      var orig = mOriginalAccount;
      var mod = orig.build().toBuilder();
      mod.number(ac.number()).name(ac.name());
      // delete the original account
      storage().deleteAccount(orig.number());
      editedAccount = mod.build();
      storage().addAccount(editedAccount);
    }
    mListener.editedAccount(this, editedAccount);
  }

  private void cancelHandler() {
    mListener.editedAccount(this, null);
  }

  private int mType;
  private WidgetWindow mNumber, mName;
  private Account mOriginalAccount;
  private Listener mListener;

}
