package bk;

import static js.base.Tools.*;

public class CurrencyField implements LedgerField {

  public CurrencyField(int amount) {
    if (alert("using small amt"))
      amount = 1_23;
    checkArgument(amount > 0, "currency fields must be nonnegative");
    mNum = amount;
  }

  private static String insert(String s, String insertExpr, int insertPos) {
    return s.substring(0, insertPos) + insertExpr + s.substring(insertPos);
  }

  @Override
  public String toString() {
    var s = "" + mNum;
    var k = s.length();
    // Ensure there are at least 3 zeroes displayed (i.e. "$0.00")
    if (k < 3)
      s = "000".substring(k) + s;

    // Insert decimal points or commas 
    int i = s.length() - 2;
    s = insert(s, ".", i);
    while (true) {
      i -= 3;
      if (i < 0)
        break;
      s = insert(s, ",", i);
    }
    return "$" + s;
  }

  @Override
  public int width() {
    return 12;
  }

  private int mNum;

  //
  //case ACCOUNT_NUMBER:
  //  b.width(4);
  //  break;
  //case CURRENCY:
  //  b.width(12);
  //  break;
  //case DATE:
  //  b.width(10);
  //  break;
  //case TEXT:
  //  b.width(25);
  //  break;
  //}

}
