package bk;

import static bk.Util.*;
import static js.base.Tools.*;

public class TransactionDescriptionHelper extends WidgetHelper {

  @Override
  public String constructHint(String prefix) {
    var hint = tri().query(prefix);
    log("hint for", quote(prefix), INDENT, quote(hint));
    if (hint.length() < 4)
      return "";
    return hint;
  }

  private Trie tri() {
    if (sTri == null) {
      var t = new Trie();
      var trs = storage().readAllTransactions();
      for (var tr : trs) {
        t.addSentence(tr.description(), null);
      }
      sTri = t;
      todo("have change listener update the tri");
    }
    return sTri;
  }

  private static Trie sTri;
}
