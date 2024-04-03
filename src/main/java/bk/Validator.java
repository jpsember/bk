package bk;

public interface Validator {

  default ValidationResult validate(String value) {
    return ValidationResult.NONE;
  }

  /**
   * Convert a value from its internal representation to its textual
   * representation
   */
  default String encode(Object data) {
    var out = "";
    if (data != null)
      out = data.toString();
    return out;
  }
}
