package bk;

import static bk.Util.*;
public class CurrencyField implements LedgerField {

  public CurrencyField(long amount) {
    mAmount = amount;
  }

  private static String insert(String s, String insertExpr, int insertPos) {
    return s.substring(0, insertPos) + insertExpr + s.substring(insertPos);
  }

  @Override
  public String toString() {
    var s = formatCurrency(mAmount);
    return s;
  }

  private long mAmount;

}
