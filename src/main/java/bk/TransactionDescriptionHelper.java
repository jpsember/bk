package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;
import java.util.Map;

import bk.gen.Transaction;

public class TransactionDescriptionHelper extends WidgetHelper {

  @Override
  public String constructHint(String prefix) {
    var hint = tri().query(prefix);
    if (verbose())
      log("hint for", quote(prefix), INDENT, quote(hint));
    if (hint.length() < 4)
      return "";
    return hint;
  }

  private Trie tri() {
    if (sTri == null) {
      sDescTransMap = hashMap();
      var t = new Trie();
      var trs = storage().readAllTransactions();
      for (var tr : trs) {
        if (tr.description().isEmpty())
          continue;
        updateDescMap(tr);
        t.addSentence(tr.description(), null);
      }
      sTri = t;
      changeManager()
          .addListener((List<Integer> modifiedAccountNumbers, List<Long> modifiedTransactionTimestamps) -> {
            for (var tt : modifiedTransactionTimestamps) {
              var tr = storage().transaction(tt);
              if (tr == null)
                return;
              sTri.addSentence(tr.description(), null);
              updateDescMap(tr);
            }
          });
    }
    return sTri;
  }

  private void updateDescMap(Transaction tr) {
    var d = normalizeDesc(tr.description());
    sDescTransMap.put(d, id(tr));
  }

  private static String normalizeDesc(String desc) {
    return desc.trim().toLowerCase();
  }

  public Transaction transactionForDescription(String d) {
    tri(); // make sure map is initialized
    var norm = normalizeDesc(d);
    var id = sDescTransMap.get(norm);
    if (id == null)
      return null;
    return storage().transaction(id);
  }

  private static Trie sTri;
  private static Map<String, Long> sDescTransMap;
}
