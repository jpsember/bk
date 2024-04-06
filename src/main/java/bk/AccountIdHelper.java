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
    log("constructHint, prefix:", quote(prefix));

    List<AccEntry> candidates = arrayList();
    var accounts = storage().accounts();

    Integer parsedAccountNumber = null;
    try {
      var c = prefix.charAt(0);
      if (c >= '1' && c <= '5') {
        parsedAccountNumber = Integer.parseInt(prefix);
      }
    } catch (Throwable t) {
    }
    int accMin = 0;
    int accMax = 9999;

    if (parsedAccountNumber != null && parsedAccountNumber > 0) {
      accMin = parsedAccountNumber;
      checkState(accMin > 0);
      accMax = accMin + 1;
      while (accMin < 1000) {
        accMin *= 10;
        accMax *= 10;
      }
      log("parsed as account number:", parsedAccountNumber, "with bounds:", accMin, accMax);

      for (var ac : accounts.values()) {
        if (ac.number() >= accMin && ac.number() < accMax) {
          var ent = accEntry(ac);
          log("account has prefix, adding:", ent);
          candidates.add(ent);
        }
      }
      //      
      //      var numStr = Integer.toString(ac.number());
      //      //log("considering account:", numStr);
      //      if (hasPrefix(numStr, prefix)) {
      //        var h = buildHint(ac);
      //        log("account has prefix, adding:", h);
      //        candidates.add(h);
      //      }
    }
    log("candidate results:", INDENT, candidates);
    var result = "";

    if (!candidates.isEmpty()) {
      candidates.sort(AccEntry.COMPARATOR);
      result = first(candidates).toString();
    }
    return result;
  }

  private static AccEntry accEntry(Account a) {
    var ent = new AccEntry();
    ent.account = a;
    return ent;
  }

  private static class AccEntry {
    private static final Comparator<AccEntry> COMPARATOR = (a, b) -> {
      return Integer.compare(a.account.number(), b.account.number());
    };

    @Override
    public String toString() {
      if (mIdWithName == null)
        mIdWithName = account.number() + " " + account.name();
      return mIdWithName;
    }

    String mIdWithName;
    Account account;
  }

}
