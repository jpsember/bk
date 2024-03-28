package bk;

public class AccountName implements LedgerField {

  public AccountName(String name) {
    mName = name;
  }

  @Override
  public String toString() {
    return mName;
  }

  @Override
  public int width() {
    return 20;
  }

  private String mName;

}
