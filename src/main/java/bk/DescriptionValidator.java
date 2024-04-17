package bk;

import static js.base.Tools.*;

import js.base.BaseObject;
import static bk.Util.*;

public class DescriptionValidator extends BaseObject implements Validator {

  public ValidationResult validate(String value) {
    var result = ValidationResult.NONE;
    //alertVerbose();
    log("validating description:", quote(value));
    value = value.trim();
    try {
      log("parsing:", value);
      if (value.length() > 1000)
        badArg("description is too long");
      for (int i = 0; i < value.length(); i++) {
        var j = value.charAt(i);
        if (j < ' ' || j > 127)
          badArg("unexpected character:", j);
      }

      var si = parseShareInfo(value);
      value = encodeShareInfo(si);

      result = new ValidationResult(value, value);
    } catch (Throwable t) {
      log("failed to validate:", quote(value), "got:", t);
    }
    return result;
  }

}
