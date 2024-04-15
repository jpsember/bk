package bk;

import static js.base.Tools.*;
import static bk.Util.*;

public class DateValidator implements Validator {

  /**
   * Encode a long to a string
   */
  public String encode(Object value) {
    var out = "";
    if (value != null) {
      var epochSeconds = (Long) value;
      out = epochSecondsToDateString(epochSeconds);
    }
    return out;
  }

  public ValidationResult validate(String value) {
    final boolean db = false && alert("db is on for DATE_VALIDATOR");
    if (db)
      pr("validating:", quote(value));

    long dateInSeconds = 0;
    String strDate = "";
    try {
      dateInSeconds = dateToEpochSeconds(value);
      strDate = epochSecondsToDateString(dateInSeconds);
    } catch (Throwable t) {
      if (db)
        pr("failed validating:", value, "got:", INDENT, t);
    }
    var result = new ValidationResult(strDate, dateInSeconds);
    if (db)
      pr("result:", result);
    return result;
  }

}
