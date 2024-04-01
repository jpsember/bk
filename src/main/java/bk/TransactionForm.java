package bk;

import static js.base.Tools.*;

import bk.gen.Transaction;

import static bk.Util.*;

public class TransactionForm extends FormWindow {

  private static final int TYPE_ADD = 0, TYPE_EDIT = 1;

  private TransactionForm(int type) {
    todo("!set date to current date if empty");
    loadTools();
    loadUtil();

    mType = type;
    mSizeExpr = 12;

   mdate =  validator(DATE_VALIDATOR).addField("Date");
  mamount=  validator(CURRENCY_VALIDATOR).addField("Amount");
  mdr=  validator(ACCOUNT_VALIDATOR).addField("Dr");
  mcr=  validator(ACCOUNT_VALIDATOR).addField("Cr");
  mdesc=  validator(DESCRIPTION_VALIDATOR).addField("Description");
    addVertSpace(1);
    addButton("Ok", () -> okHandler());
    addButton("Cancel", () -> cancelHandler());
    addVertSpace(1);
    addMessageLine();
  }

  public static void addTransaction() {
    var f = new TransactionForm(TYPE_ADD);
    // Add transaction window to main
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
    
    todo("have validator return Objects (as well as ability to convert those objects to strings)");
  //  mdate.value();
    
    if (mType == TYPE_ADD) {
      setMessage("Ok pressed!");
    }
 //   removeFormFromScreen();
  }

  private void cancelHandler() {
    removeFormFromScreen();
  }

  private int mType;
  private FocusHandler mOldFocus;
  private WidgetWindow mdate,mamount,mdr,mcr,mdesc;
  
}
