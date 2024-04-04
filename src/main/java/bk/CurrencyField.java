package bk;

import static bk.Util.*;

public class CurrencyField implements LedgerField {

  public CurrencyField(long amount) {
    mAmount = amount;
  }

  @Override
  public String toString() {
    var s = formatCurrency(mAmount);
    return s;
  }

  private long mAmount;

}
