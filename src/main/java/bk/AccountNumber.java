package bk;

public class AccountNumber implements LedgerField {

  public AccountNumber(int number) {
    mNum = number;
  }

  @Override
  public String toString() {
    return "" + mNum;
  }

  @Override
  public int width() {
    return 4;
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
