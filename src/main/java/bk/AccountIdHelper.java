package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.Comparator;
import java.util.List;

import bk.gen.Account;

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
      for (var a : ac) {
        t.addSentence("" + a.number());
        t.addSentence(a.name());
      }
      for (var a : ac) {
        t.addWords(a.name());
      }
      sTri = t;
    }
    return sTri;
  }

  private static Tri sTri;
}
