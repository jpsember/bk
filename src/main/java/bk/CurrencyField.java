package bk;

import static js.base.Tools.*;

public class CurrencyField implements LedgerField {

  public CurrencyField(int amount) {
    checkArgument(amount > 0, "currency fields must be nonnegative");
    mNum = amount;
  }

  @Override
  public String toString() {
    var s = "" + mNum;
    var k = s.length();
    if (k < 3)
      s = "000".substring(k) + s;
    int i = s.length();
    s = s.substring(0, i - 2) + "." + s.substring(i - 2);

    // Position at the period
    i = s.length() - 2;
    while (i > 3) {
      s = s.substring(0, i - 3) + "," + s.substring(i - 3);
      i -= 4;
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
