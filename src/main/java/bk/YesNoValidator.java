package bk;

import static js.base.Tools.*;

import js.base.BaseObject;

public class YesNoValidator extends BaseObject implements Validator {

  public ValidationResult validate(String value) {
    loadTools();
    var result = ValidationResult.NONE;
    value = value.trim().toLowerCase();
    boolean b = false;
    if (value.equals("yes") || value.equals("y"))
      b = true;
    var valueAsString = encode(b);
    result = new ValidationResult(valueAsString, b);
    log("returning:", INDENT, result);
    return result;
  }

  @Override
  public String encode(Object data) {
    boolean f = false;
    if (data != null)
      f = (Boolean) data;
    return f ? "yes" : "no";
  }
}
