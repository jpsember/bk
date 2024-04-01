package bk;

import static js.base.Tools.*;
import static bk.Util.*;

public class TransactionForm extends FormWindow {

  public TransactionForm() {
    loadTools();
    loadUtil();
    validator(DATE_VALIDATOR).addField("Date");
    validator(CURRENCY_VALIDATOR).addField("Amount");
    validator(ACCOUNT_VALIDATOR).addField("Dr");
    validator(ACCOUNT_VALIDATOR).addField("Cr");
    validator(DESCRIPTION_VALIDATOR).addField("Description");

  }
}
