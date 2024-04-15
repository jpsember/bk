package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.Account;

public class AccountIdHelper extends WidgetHelper implements ChangeListener {

  @Override
  public String constructHint(String prefix) {
    var hint = tri().query(prefix);
    log("hint for", quote(prefix), INDENT, quote(hint));
    return hint;
  }

  private Trie tri() {
    if (sTri == null) {
      var t = new Trie();
      var ac = storage().readAllAccounts();
      for (var a : ac) {
        addAccountInfo(t, a);
      }
      sTri = t;
      changeManager().addListener(this);
    }
    return sTri;
  }

  private void addAccountInfo(Trie t, Account a) {
    var output = accountNumberWithNameString(a);
    t.addSentence("" + a.number(), output);
    t.addSentence(a.name(), output);
  }

  private static Trie sTri;

  @Override
  public void dataChanged(List<Integer> modifiedAccountNumbers, List<Long> modifiedTransactionTimestamps) {
    for (var aa : modifiedAccountNumbers) {
      var a = storage().account(aa);
      addAccountInfo(tri(), a);
    }
  }
}
