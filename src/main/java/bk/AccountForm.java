package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import bk.gen.Account;

public class AccountForm extends FormWindow {

  private static final int TYPE_ADD = 0, TYPE_EDIT = 1;

  private AccountForm(int type) {
    loadTools();
    mType = type;
    mSizeExpr = 12;

    //mdate = validator(DATE_VALIDATOR).addField("Date");
    // mamount = validator(CURRENCY_VALIDATOR).addField("Amount");
    mNumber = validator(ACCOUNT_VALIDATOR).addField("#");
    mName = validator(ACCOUNT_NAME_VALIDATOR).fieldWidth(ACCOUNT_NAME_MAX_LENGTH).addField("Name");
    addVertSpace(1);
    addButton("Ok", () -> okHandler());
    addButton("Cancel", () -> cancelHandler());
    addVertSpace(1);
    addMessageLine();
  }

  public static void addAccount() {
    var f = new AccountForm(TYPE_ADD);
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
        var existing = storage().account(ac.number());
        if (existing != null)
          problem = "That account number is taken!";
      }
        break;
      case TYPE_EDIT:
        break;
      }
    }
    if (problem != null) {
      setMessage(problem);
      return;
    }

    // Add an account
    storage().addAccount(ac);

    todo("redraw the accounts list");

    removeFormFromScreen();
  }

  private void cancelHandler() {
    removeFormFromScreen();
  }

  private int mType;
  private FocusHandler mOldFocus;
  private WidgetWindow mNumber, mName;
}
