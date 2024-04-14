package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;


public class AccountIdHelper extends WidgetHelper {

  @Override
  public String constructHint(String prefix) {
    alertVerbose();
    var hint = tri().query(prefix);
    log("hint for", quote(prefix), INDENT, quote(hint));
    return hint;
  }

  private Tri tri() {
    if (sTri == null) {
      var t = new Tri();
      var ac = storage().readAllAccounts();
      List<String> outputSentences = arrayList();
      for (var a : ac) {
        var output = accountNumberWithNameString(a);
        outputSentences.add(output);
        t.addSentence("" + a.number(), output);
        t.addSentence(a.name(), output);
      }
      for (var s : outputSentences)
        t.addWords(s);
      sTri = t;
      todo("have change listener update the tri");
    }
    return sTri;
  }

  private static Tri sTri;
}
