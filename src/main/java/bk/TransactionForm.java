package bk;

import static js.base.Tools.*;
import static bk.Util.*;

public class TransactionForm extends FormWindow {

  public TransactionForm(TransactionHandler handler) {
    todo("!set date to current date if empty");
    loadTools();
    loadUtil();
    mHandler = handler;

    todo("!hide cursor when focus changes");
    validator(DATE_VALIDATOR).addField("Date");
    validator(CURRENCY_VALIDATOR).addField("Amount");
    validator(ACCOUNT_VALIDATOR).addField("Dr");
    validator(ACCOUNT_VALIDATOR).addField("Cr");
    validator(DESCRIPTION_VALIDATOR).addField("Description");
    addVertSpace(1);
    addButton("Ok");
    addButton("Cancel");
  }

  /* private */ TransactionHandler mHandler;

}
