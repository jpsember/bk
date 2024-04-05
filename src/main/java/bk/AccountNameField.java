package bk;

public class AccountNameField implements LedgerField {

  public AccountNameField(String name) {
    this(0, name);
  }

  public AccountNameField(int number, String name) {
    mName = name;
    mNumber = number;
  }

  @Override
  public String toString() {
    if (mNumber != 0)
      return mNumber + " " + mName;
    return mName;
  }

  private String mName;
  private int mNumber;
}
