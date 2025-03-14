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

    // Split it into up into two sections:
    // 1) the number
    // 2) anything following the first space or colon, to treat as a potential name

    String numberStr, nameStr;
    // If there is anything following the first space or ":", replace the whole
    // thing with the account number + name.
    {
      int i = value.indexOf(' ');
      if (i < 0)
        i = value.indexOf(':');
      if (i < 0)
        i = value.length();

      numberStr = value.substring(0, i).trim();

      var s = chompPrefix(value.substring(i), ":").trim();
      s = chomp(s, "???").trim();
      nameStr = s;
    }
    try {
      var i = Integer.parseInt(numberStr);
      if (i < 1000 || i > 5999)
        throw badArg("unexpected account number", i);

      Integer intRes = i;
      String s;
      if (mForNewAccountFlag && accountExists(i)) {
        s = numberStr + " !!! Already exists";
        intRes = null;
      } else {
        s = accountNumberWithNameString(i, nameStr);
      }
      result = new ValidationResult(s, intRes);
      result.setExtraString(nameStr);
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
          var acct = storage().account(ival);
          if (acct != null)
            out = accountNumberWithNameString(acct);
          else
            out = Integer.toString(ival);
          log("encode", db(ival), "to", db(out));
        }
      }
    }
    return out;
  }

  public AccountValidator withForNewAccount(boolean b) {
    mForNewAccountFlag = b;
    return this;
  }

  public boolean mForNewAccountFlag;

}
