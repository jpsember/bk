package bk;

import static js.base.Tools.*;
import static bk.Util.*;

public class DateField implements LedgerField {

  public DateField(int epochSeconds) {
    loadTools();
    mEpochSec = epochSeconds;
  }

  @Override
  public String toString() {
    if (mStr == null) {
      mStr = formatDate(mEpochSec);
      pr("formatted date", mEpochSec, "to:", mStr);
    }
    return mStr;
  }

  @Override
  public int width() {
    return 10;
  }

  private int mEpochSec;
  private String mStr;

}
