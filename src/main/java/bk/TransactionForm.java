package bk;

import static js.base.Tools.*;
import static bk.Util.*;

public class TransactionForm extends FormWindow {

  public TransactionForm(TransactionHandler handler) {
    todo("set date to current date if empty");
    loadTools();
    loadUtil();
    mHandler = handler;
    validator(DATE_VALIDATOR).addField("Date");
    validator(CURRENCY_VALIDATOR).addField("Amount");
    validator(ACCOUNT_VALIDATOR).addField("Dr");
    validator(ACCOUNT_VALIDATOR).addField("Cr");
    validator(DESCRIPTION_VALIDATOR).addField("Description");
  }

  /*private*/ TransactionHandler mHandler;

}
