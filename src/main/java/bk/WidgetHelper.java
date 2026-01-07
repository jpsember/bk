package bk;

import static js.base.Tools.*;

import static bk.Util.*;

import java.util.Map;

import js.base.BaseObject;

public abstract class WidgetHelper extends BaseObject {

  /**
   * Construct a hint for a prefix (which will be a nonempty lower case string)
   */
  public abstract String constructHint(String prefix);

  public final String getHint(String prefix) {
    d84   ("Widget Helper, get hint for prefix:", quote(prefix));
    loadUtil();
    String hint = "";
    if (!prefix.isEmpty()) {
      var prefixLower = prefix.toLowerCase();
      hint = nullToEmpty(constructHint(prefix));
      d84  ("...hint for prefix:", prefix, "is:", hint);
    }
    return hint;
  }

  // A map of user prefix (in lower case) and the hint that should be shown for that prefix (which might be an empty string)
  private Map<String, String> mHintResultsMap = hashMap();

}
