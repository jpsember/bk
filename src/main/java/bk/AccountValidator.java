package bk;

import static js.base.Tools.*;
import static bk.Util.*;
import js.base.BaseObject;

public class AccountValidator extends BaseObject implements Validator {

  public AccountValidator() {
    //alertVerbose();
  }

  public ValidationResult validate(String value) {
    var result = ValidationResult.NONE;
    log("validating account number:", value);

    value = value.trim();
    value = chomp(value, "???");
    try {
      log("parsing:", value);
      var i = Integer.parseInt(value);
      if (i < 1000 || i > 5999)
        throw badArg("unexpected account number", i);
      result = new ValidationResult(Integer.toString(i), i);
    } catch (Throwable t) {
      log("failed to validate:", quote(value), "got:", t);
    }
    return result;
  }

  @Override
  public String encode(Object value) {
    var ival = (Integer) value;
    var out = "";
    
    if (ival != null && ival != 0) {
      out = Integer.toString(ival);
      log("encode", db(ival), "to", db(out));
    }
    return out;
  }
}
