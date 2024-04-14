package bk;

import static bk.Util.*;
import static js.base.Tools.*;

public class AccountIdHelper extends WidgetHelper {

  @Override
  public String constructHint(String prefix) {
    alertVerbose();
    var hint = tri().query(prefix);
    log("hint for", quote(prefix), INDENT, quote(hint));
    return hint;
  }

  private Trie tri() {
    if (sTri == null) {
      var t = new Trie();
      var ac = storage().readAllAccounts();
      for (var a : ac) {
        var output = accountNumberWithNameString(a);
        t.addSentence("" + a.number(), output);
        t.addSentence(a.name(), output);
      }
      sTri = t;
      todo("have change listener update the tri");
    }
    return sTri;
  }

  private static Trie sTri;
}
