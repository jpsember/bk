package bk;

public class AccountNameField implements LedgerField {

  public AccountNameField(String name) {
    mName = name;
  }

  @Override
  public String toString() {
    return mName;
  }

  private String mName;

}
