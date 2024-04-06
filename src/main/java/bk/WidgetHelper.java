package bk;

import static js.base.Tools.*;

import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import js.base.BaseObject;

public abstract class WidgetHelper extends BaseObject {

  public WidgetHelper() {
    alertVerbose();
  }

  /**
   * Construct a list of user prefixes, and corresponding hints that should be
   * shown for those prefixes
   */
  public abstract void constructHintResults(List<String> userPrefixList, List<String> hintList);

  public String getHint(String prefix) {
    String hint = "";
    outer: do {
      if (!prefix.isEmpty()) {
        var tl = hintResults().tailMap(prefix);
        if (tl.isEmpty())
          break;
        var x = tl.get(tl.firstKey());
        if (x.length() < prefix.length())
          break;
        if (String.CASE_INSENSITIVE_ORDER.compare(prefix, x.substring(0, prefix.length())) != 0)
          break;
        hint = x;
        break outer;
      }
    } while (false);
    return hint;
  }

  private SortedMap<String, String> hintResults() {
    if (mHintResultsMap == null) {
      List<String> userPrefixList = arrayList();
      List<String> hintList = arrayList();

      constructHintResults(userPrefixList, hintList);
      checkState(hintList.size() == userPrefixList.size(), "hint result arrays not same size");

      SortedMap<String, String> mp = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

      int i = INIT_INDEX;
      for (var pref : userPrefixList) {
        i++;
        var hint = hintList.get(i);
        mp.put(pref, hint);
      }
      mHintResultsMap = mp;
    }
    return mHintResultsMap;
  }

  private SortedMap<String, String> mHintResultsMap;
}
