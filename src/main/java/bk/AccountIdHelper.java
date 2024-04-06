package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.Comparator;
import java.util.List;

import bk.gen.Account;

public class AccountIdHelper extends WidgetHelper {

  @Override
  public String constructHint(String prefix) {
    //alertVerbose();
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

    // If prefix is a (positive) number, look for corresponding account numbers

    if (parsedAccountNumber != null) {
      int accMin = 0;
      int accMax = 9999;
      accMin = parsedAccountNumber;
      checkState(accMin > 0);
      accMax = accMin + 1;
      while (accMin < 1000) {
        accMin *= 10;
        accMax *= 10;
      }
      log("parsed as account number:", parsedAccountNumber, "with bounds:", accMin, accMax);

      for (var account : accounts.values()) {
        if (account.number() >= accMin && account.number() < accMax) {
          var ent = accEntry(account);
          log("account has prefix, adding:", ent);
          candidates.add(ent);
        }
      }
    } else {
      // If any account name words match this prefix, or start with the prefix, make them candidates.
      // Favor account names that contain the prefix as a single word.

      List<AccEntry> prefCandidates = arrayList();

      outer: for (var account : accounts.values()) {
        var name = account.name();
        var words = split(name, ' ');
        for (var w : words) {
          if (matchCaseInsens(w, prefix)) {
            candidates.add(accEntry(account));
            continue outer;
          } else if (candidates.isEmpty() && hasPrefix(w, prefix)) {
            prefCandidates.add(accEntry(account));
            continue outer;
          }
        }
      }
      if (candidates.isEmpty())
        candidates.addAll(prefCandidates);
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
