package bk;

import static js.base.Tools.*;
import static bk.Util.*;

public class ValidationResult {

  public ValidationResult(String stringValue, Object validatedValue) {
    mStringValue = nullToEmpty(stringValue);
    mValidatedValue = validatedValue;
  }

  @Override
  public String toString() {
    var m = map();
    m.put("string", mStringValue);
    m.put("value", db(mValidatedValue));
    return m.toString();
  }

  private String mStringValue;
  private Object mValidatedValue;

  public String string() {
    return mStringValue;
  }

  public Object value() {
    return mValidatedValue;
  }

  public <T> T typedValue() {
    return (T) value();
  }

  public static final ValidationResult NONE = new ValidationResult(null, null);

}
