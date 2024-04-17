package bk;

import static js.base.Tools.*;

import js.base.BaseObject;

public class BoolValidator extends BaseObject implements Validator {

  public ValidationResult validate(String value) {
    loadTools();
    var result = ValidationResult.NONE;
    //    alertVerbose();
    log("validate bool:", value);

    value = value.trim().toLowerCase();

    log("parsing:", value);
    boolean b = false;
    if (value.equals("yes") || value.equals("y") || value.equals("t") || value.equals("true"))
      b = true;
    result = new ValidationResult(b ? "yes" : "no", b);

    return result;
  }

}
