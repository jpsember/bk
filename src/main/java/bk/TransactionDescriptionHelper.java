package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

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
      changeManager()
          .addListener((List<Integer> modifiedAccountNumbers, List<Long> modifiedTransactionTimestamps) -> {
            for (var tt : modifiedTransactionTimestamps) {
              var tr = storage().transaction(tt);
              sTri.addSentence(tr.description(), null);
            }
          });
    }
    return sTri;
  }

  private static Trie sTri;
}
