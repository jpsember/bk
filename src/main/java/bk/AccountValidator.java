package bk;

import static js.base.Tools.*;
import static bk.Util.*;
import js.base.BaseObject;

public class AccountValidator extends BaseObject implements Validator {

  public ValidationResult validate(String value) {
    //alertVerbose();
    var result = ValidationResult.NONE;
    log("validating account number (or name):", value);

    value = value.trim();

    // If there is anything following the first space or ":", replace the whole
    // thing with the account number + name.
    {
      int i = value.indexOf(' ');
      if (i < 0)
        i = value.length();
      int j = value.indexOf(':');
      if (j < 0)
        j = value.length();
      value = value.substring(0, Math.min(i, j));
      log("trimmed name:", value);
    }
    try {
      log("parsing:", value);
      var i = Integer.parseInt(value);
      if (i < 1000 || i > 5999)
        throw badArg("unexpected account number", i);
      result = new ValidationResult(accountNumberWithNameString(i, false), i);
    } catch (Throwable t) {
      log("failed to validate:", quote(value), "got:", t);
    }

    return result;
  }

  @Override
  public String encode(Object value) {
    var out = "";
    if (value != null) {
      if (value instanceof CharSequence) {
        out = value.toString();
      } else {
        var ival = (Integer) value;
        if (ival != 0) {
          out = Integer.toString(ival);
          log("encode", db(ival), "to", db(out));
        }
      }
    }
    return out;
  }
}
