package bk;

import js.base.BaseObject;

import static js.base.Tools.*;
import static js.base.Tools.INDENT;

public class ShortcutValidator extends BaseObject implements Validator {

  public ValidationResult validate(String value) {
    loadTools();
    setVerbose();
    final boolean db = false && alert("db is on");

    if (db)
      pr("validating shortcut:", value);
    var result = ValidationResult.NONE;
    try {
      value = value.trim();
      // We will append a suffix to indicate if the shortcut is already used...

      //      checkState(value.length() <= 1);
      if (!value.isEmpty()) {
        checkState(
            Character.isLetter(value.charAt(0)));
      }
      var valueAsString = encode(value);
      result = new ValidationResult(valueAsString, valueAsString);
    } catch (Throwable t) {
      if (db)
        pr("failed to validate:", quote(value), "got:", t);
    }
    if (db)
      pr("returning:", INDENT, result);
    return result;
  }

}
