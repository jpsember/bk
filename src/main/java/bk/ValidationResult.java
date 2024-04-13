package bk;

import static js.base.Tools.*;
import static bk.Util.*;

public class ValidationResult {

  public ValidationResult(String stringValue, Object validatedValue) {
    mStringValue = nullToEmpty(stringValue);
    mValidatedValue = validatedValue;
  }

  public void setExtraString(String s) {
    mExtraStringValue = nullToEmpty(s);
  }

  @Override
  public String toString() {
    var m = map();
    m.put("string", mStringValue);
    m.put("value", db(mValidatedValue));
    if (nonEmpty(extraString()))
      m.put("string_extra", extraString());
    return m.toString();
  }

  private String mStringValue;
  private Object mValidatedValue;
  private String mExtraStringValue;

  public String string() {
    return mStringValue;
  }

  public Object value() {
    return mValidatedValue;
  }

  public String extraString() {
    return nullToEmpty(mExtraStringValue);
  }

  public <T> T typedValue() {
    return (T) value();
  }

  public static final ValidationResult NONE = new ValidationResult(null, null);

}
