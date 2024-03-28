package bk;

import static js.base.Tools.*;

public class TransactionDescriptionField implements LedgerField {

  public TransactionDescriptionField(String desc) {
    loadTools();
    mDesc = desc;
  }

  @Override
  public String toString() {
    return mDesc;
  }

  @Override
  public int width() {
    return 25;
  }

  private String mDesc;

}
