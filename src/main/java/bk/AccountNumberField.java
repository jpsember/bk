package bk;

public class AccountNumberField implements LedgerField {

  public AccountNumberField(int number) {
    mAccountNumber = number;
  }

  @Override
  public String toString() {
    return Integer.toString(mAccountNumber);
  }

  private final int mAccountNumber;

}
