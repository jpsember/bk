package bk;

import static js.base.Tools.*;
import static bk.Util.*;

public class DateField implements LedgerField {

  public DateField(long epochSeconds) {
    loadTools();
    mEpochSeconds = epochSeconds;
  }

  @Override
  public String toString() {
    if (mString == null) {
      mString = formatDate(mEpochSeconds);
    }
    return mString;
  }

  private long mEpochSeconds;
  private String mString;

}
