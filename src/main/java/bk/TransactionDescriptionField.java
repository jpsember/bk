package bk;

public class TransactionDescriptionField implements LedgerField {

  public TransactionDescriptionField(String desc) {
    mText = desc;
  }

  @Override
  public String toString() {
    return mText;
  }

  private String mText;

}
