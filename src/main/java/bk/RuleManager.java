package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import bk.gen.Account;
import bk.gen.Transaction;
import bk.gen.rules.Rule;
import bk.gen.rules.Rules;
import js.base.BaseObject;
import js.data.DataUtil;
import js.file.Files;
import js.json.JSMap;
import js.parsing.RegExp;

public class RuleManager extends BaseObject {

  public static final RuleManager SHARED_INSTANCE = new RuleManager();

  private RuleManager() {
    alertVerbose();
  }

  public void applyRules(Collection<Integer> accountIds) {
    loadTools();
    if (accountIds.isEmpty())
      return;
    log("applying rules, accounts:", accountIds);

    // Invalidate our account ledger cache
    mAccountLedgerCache.clear();
    // We don't need to clear the child transaction cache if we are careful to only generate
    // fresh parent transactions

    for (var accountId : accountIds) {
      var account = storage().account(accountId);
      if (account == null) {
        log("...account doesn't exist:", accountId);
        continue;
      }
      mParentAccount = account;
      for (var tr : getAccountTransactions(account.number())) {
        if (isGenerated(tr))
          continue;
        mParent = tr;
        mNewChildren.clear();
        applyRules();
        
      }
    }
  }

  private static long[] copyOf(long[] source) {
    if (source.length == 0)
      return source;
    todo("put this in DataUtil");
    return Arrays.copyOf(source, source.length);
  }

  /**
   * Apply any rules to the current transaction
   */
  private void applyRules() {
    // We need to keep a list of any *existing* generated 'child' transactions of this one,
    // so that we 1) avoid re-generating an otherwise identical transaction, and 
    //            2) delete any stale generated transaction
    //

    for (var entry : rules().rules().entrySet()) {
      var rule = entry.getValue();
      if (!rule.accounts().contains(mParentAccount.number()))
        continue;
      applyRule(rule);
    }
  }

  private void applyRule(Rule rule) {
    if (!rule.conditions().isEmpty())
      alert("ignoring rule conditions for now:", INDENT, rule);

    for (var actionMap : rule.actions()) {
      String actionName = actionMap.opt("action", "generate");

      switch (actionName) {
      default:
        alert("unsupported action:", actionMap);
        break;
      case "generate":
        applyGenerateRule(actionMap);
        break;
      }
    }

  }

  private void applyGenerateRule(JSMap actionMap) {

    var parent = mParent;
    int debitAccountNum = determineAccountNumber(mParentAccount, actionMap, "debit", mParentAccount.number());
    int creditAccountNum = determineAccountNumber(mParentAccount, actionMap, "credit",
        mParentAccount.number());

    if (debitAccountNum == creditAccountNum) {
      alert("debit = credit account numbers =", debitAccountNum, "while applying rule, map:", INDENT,
          actionMap);
      return;
    }

    var amount = determineTransactionAmount(parent, actionMap, "amount");

    // If there is already a generated transaction in the child list matching this one, do nothing
    var existingTr = findChildTransaction(debitAccountNum, creditAccountNum, amount);
    if (existingTr == null) {
      log("...a child transaction already exists");
      return;
    }

    var tr = Transaction.newBuilder();

    tr.timestamp(parent.timestamp());
    tr.date(parent.date());
    tr.amount(amount);
    tr.debit(debitAccountNum);
    tr.credit(creditAccountNum);
    tr.description("(generated) " + parent.description());
    mNewChildren.add(tr.build());
  }

  /**
   * Determine if the parent transaction already has an equivalent generated
   * transaction
   */
  private Transaction findChildTransaction(int dr, int cr, long amount) {
    var parent = mParent;
    var lst = getChildTransactions(parent);
    for (var t : lst) {
      if (t.debit() == dr && t.credit() == cr && t.amount() == amount) {
        return t;
      }
    }
    return null;
  }

  private int determineAccountNumber(Account sourceAccount, JSMap map, String key, Integer defaultValue) {
    Object val = map.optUnsafe(key);
    if (val == null) {
      checkNotNull(defaultValue, "no default account number provided for rule, key:", key, "map:", INDENT,
          map);
      return defaultValue;
    }
    if (val instanceof Number) {
      return ((Number) val).intValue();
    }
    throw badArg("unexpected value:", val, "for key", key, "in map:", INDENT, map);
  }

  private long determineTransactionAmount(Transaction parentTransaction, JSMap map, String key) {
    Object val = map.optUnsafe(key);
    if (val == null) {
      throw badArg("missing key:", key, "in map:", INDENT, map);
    }
    Long amountInCents = null;
    if (val instanceof String) {
      var s = (String) val;
      if (RegExp.patternMatchesString("(\\d+[.]\\d*)%", s)) {
        double pct = Double.parseDouble(s.substring(0, s.length() - 1));
        amountInCents = Math.round(parentTransaction.amount() * (pct / 100));
      } else {
        badArg("for now, expected percentage, e.g. '33.333%'");
        amountInCents = Math.round(Double.parseDouble(s) * 100); // x 100 cents per dollar 
      }
    }
    if (amountInCents == null)
      badArg("can't figure out transaction amount, key:", key, INDENT, map);
    return amountInCents;
  }

  private Rules rules() {
    if (mRules == null) {
      mRules = Files.parseAbstractDataOpt(Rules.DEFAULT_INSTANCE, file());
    }
    return mRules;
  }

  private File file() {
    if (mFile == null) {
      var sf = storage().file();
      mFile = new File(Files.removeExtension(sf) + "_rules.json");
    }
    return mFile;
  }

  private List<Transaction> getAccountTransactions(int accountNumber) {
    var lst = mAccountLedgerCache.get(accountNumber);
    if (lst == null) {
      lst = storage().transactionsForAccount(accountNumber);
    }
    return lst;
  }

  private List<Transaction> getChildTransactions(Transaction parent) {
    if (parent.children().length == 0)
      return DataUtil.emptyList();
    var lst = mChildTransactionListCache.get(parent.timestamp());
    if (lst == null) {
      lst = arrayList();
      for (var timestamp : parent.children()) {
        var tr = storage().transaction(timestamp);
        if (tr == null) {
          alert("can't find child transaction", timestamp, "for parent:", INDENT, parent);
          continue;
        }
        lst.add(tr);
      }
      mChildTransactionListCache.put(parent.timestamp(), lst);
    }
    return lst;
  }

  private File mFile;
  private Rules mRules;
  private Map<Integer, List<Transaction>> mAccountLedgerCache = hashMap();
  private Map<Long, List<Transaction>> mChildTransactionListCache = hashMap();
  private Account mParentAccount;
  private Transaction mParent;
  private List<Transaction> mNewChildren = arrayList();
}
