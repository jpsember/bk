package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import bk.gen.Account;

public class AccountForm extends FormWindow {

  private static final int TYPE_ADD = 0, TYPE_EDIT = 1;

  private AccountForm(int type, Account account) {
    loadTools();
    account = nullTo(account, Account.DEFAULT_INSTANCE).build();
    mOriginalAccount = account;
    mType = type;
    mSizeExpr = 12;

    mNumber = validator(ACCOUNT_VALIDATOR).value(account.number()).addField("#");
    mName = validator(ACCOUNT_NAME_VALIDATOR).value(account.name()).fieldWidth(ACCOUNT_NAME_MAX_LENGTH)
        .addField("Name");
    addVertSpace(1);
    addButton("Ok", () -> okHandler());
    addButton("Cancel", () -> cancelHandler());
    addVertSpace(1);
    addMessageLine();
  }

  public static void addAccount() {
    var f = new AccountForm(TYPE_ADD, null);
    f.addFormToScreen();
  }

  public static void editAccount(Account account) {
    var f = new AccountForm(TYPE_EDIT, account);
    f.addFormToScreen();
  }

  private void addFormToScreen() {
    var m = winMgr();
    var c = m.topLevelContainer();
    c.children().add(this);
    c.setLayoutInvalid();

    var fm = focusManager();
    mOldFocus = fm.focus();
    fm.set(fm.handlers(this).get(0));
  }

  private void removeFormFromScreen() {
    var m = winMgr();
    var c = m.topLevelContainer();
    c.children().remove(this);
    c.setLayoutInvalid();
    focusManager().set(mOldFocus);
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

    if (mType == TYPE_ADD) {
      storage().addAccount(ac);
    } else {
      // modify original account to include the edits
      var orig = mOriginalAccount;
      var mod = orig.build().toBuilder();
      mod.number(ac.number()).name(ac.name());
      // delete the original account
      storage().deleteAccount(orig.number());
      storage().addAccount(mod);
    }

    todo("redraw the accounts list");

    removeFormFromScreen();
  }

  private void cancelHandler() {
    removeFormFromScreen();
  }

  private int mType;
  private FocusHandler mOldFocus;
  private WidgetWindow mNumber, mName;
  private Account mOriginalAccount;
}
