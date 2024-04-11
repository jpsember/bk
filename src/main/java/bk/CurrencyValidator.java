package bk;

import static js.base.Tools.*;
import static bk.Util.*;

public class CurrencyValidator implements Validator {

  @Override
  public String encode(Object value) {
    var out = "";
    if (value != null) {
      var i = (Long) value;
      out = formatCurrency(i);
    }
    return out;
  }

  public CurrencyValidator withCanBeZero(boolean canBeZero) {
    mCanBeZero = canBeZero;
    return this;
  }

  public ValidationResult validate(String value) {
    final boolean db = false && alert("db is on");
    if (db)
      pr("validating currency:", value);
    value = value.trim();
    value = value.replace("$", "");
    value = value.replace(",", "");
    Long amount = null;
    var result = ValidationResult.NONE;
    try {
      boolean neg = false;
      if (value.startsWith("(") && value.endsWith(")")) {
        neg = true;
        value = value.substring(1, value.length() - 1);
      } else if (value.startsWith("-")) {
        neg = true;
        value = value.substring(1);
      }

      if (!value.isEmpty()) {
        int j = value.lastIndexOf('.');
        if (j < 0) {
          value = value + ".00";
        }
      }
      if (db)
        pr("parsing:", value);
      if (mCanBeZero && value.isEmpty())
        value = "0";
      var d = Double.parseDouble(value);
      var toLong = Math.round(d * 100);
      if (toLong >= MAX_CURRENCY)
        throw badArg("failed to convert", value);
      amount = toLong;
      if (neg)
        amount = -amount;
      result = new ValidationResult(formatCurrency(amount), amount);
    } catch (Throwable t) {
      if (db)
        pr("failed to validate:", quote(value), "got:", t);
    }
    return result;
  }

  private boolean mCanBeZero;
}
