package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import bk.gen.Account;
import bk.gen.Transaction;
import bk.gen.rules.Rule;
import bk.gen.rules.Rules;
import js.base.BaseObject;
import js.data.DataUtil;
import js.data.LongArray;
import js.file.Files;
import js.json.JSMap;

public class RuleManager extends BaseObject {

  public static final RuleManager SHARED_INSTANCE = new RuleManager();

  private RuleManager() {
    alertVerbose();
  }

  @Deprecated
  public void applyRules(Collection<Integer> accountIds) {
    loadTools();
    if (accountIds.isEmpty())
      return;
    log("applying rules, accounts:", accountIds);

    // Invalidate our account ledger cache

    todo("have the cache store transaction ids only, in case we change the underlying transactions");
    mAccountLedgerCache.clear();

    // Delete *all* existing child transactions for all parents in the accounts we're generating rules for.
    List<Account> parents = arrayList();

    for (var accountId : accountIds) {
      var account = storage().account(accountId);
      if (account == null) {
        log("...account doesn't exist:", accountId);
        continue;
      }
      parents.add(account);

      var trList = getAccountTransactions(account.number());
      log("examing account:", INDENT, account);

      List<Long> trimmedList = arrayList();
      for (var tr : trList) {
        log("transaction:", INDENT, tr);
        if (isGenerated(tr)) {
          log("found a generated transaction; deleting it:", INDENT, tr);
          storage().deleteTransaction(tr);
          // Remove this transaction's id from its parent
          removeChildFromParent(tr);
        } else {
          trimmedList.add(id(tr));
        }
      }
      if (trimmedList.size() != trList.size())
        mAccountLedgerCache.put(accountId, trimmedList);
    }

    for (var parent : parents) {
      mParentAccount = parent;
      for (var tr : getAccountTransactions(parent.number())) {
        // We should *not* find any generated transactions in the cache, as we've
        // already deleted them above; as long as we haven't invalidated the cache.
        checkState(!isGenerated(tr));
        mParent = tr;
        mNewChildren.clear();
        applyRules();

        if (!mNewChildren.isEmpty()) {
          log("...we have new children for:", mParent.timestamp(), INDENT, mNewChildren);

          // Add these children to the existing list of children

          var b = mParent.toBuilder();
          var bchild = LongArray.with(b.children()).toBuilder();
          for (var ch : mNewChildren)
            bchild.add(ch.timestamp());
          b.children(bchild.array());

          log("replacing transaction:", INDENT, b);
          storage().replaceTransactionWithoutUpdatingAccountBalances(b);

          // Add the children as well
          for (var ch : mNewChildren) {
            storage().addTransaction(ch);
            changeManager().registerModifiedTransaction(ch);

          }
        }
      }
    }
  }

  /**
   * Apply any rules to the current transaction
   */
  private void applyRules() {
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
    int dr = determineAccountNumber(mParentAccount, actionMap, "debit", mParentAccount.number());
    int cr = determineAccountNumber(mParentAccount, actionMap, "credit", mParentAccount.number());

    if (dr == cr) {
      alert("debit = credit account numbers =", dr, "while applying rule, map:", INDENT, actionMap);
      return;
    }

    var amount = determineTransactionAmount(parent, actionMap, "amount");

    // If there is already a generated transaction in the child list matching this one, do nothing
    var existing = findChildTransaction(dr, cr, amount);
    if (existing != null) {
      log("...a child transaction already exists for dr:", dr, "cr:", cr, "amount:", amount, INDENT,
          existing);
      return;
    }

    var tr = Transaction.newBuilder();

    tr.timestamp(storage().uniqueTimestamp());
    tr.date(parent.date());
    tr.amount(amount);
    tr.debit(dr);
    tr.credit(cr);
    tr.description("(generated) " + parent.description());
    tr.parent(mParent.timestamp());
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
      // fucking regexes are stupidly complicated, abandoning them
      if (s.endsWith("%")) {
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
      log("read rules:", INDENT, mRules);

    }
    return mRules;
  }

  private File file() {
    if (mFile == null) {
      var sf = storage().file();
      mFile = new File(Files.removeExtension(sf) + ".rules.json");
      if (verbose())
        log("set file to:", INDENT, Files.infoMap(mFile));
    }
    return mFile;
  }

  private List<Transaction> getAccountTransactions(int accountNumber) {
    var lst = mAccountLedgerCache.get(accountNumber);
    if (lst == null) {
      var trs = storage().readTransactionsForAccount(accountNumber);
      lst = arrayList();
      for (var x : trs)
        lst.add(x.timestamp());
      mAccountLedgerCache.put(accountNumber, lst);
      return trs;
    }
    List<Transaction> trs = arrayList();
    for (var id : lst) {
      var t = storage().transaction(id);
      checkNotNull(t);
      trs.add(t);
    }
    return trs;
  }

  private List<Transaction> getChildTransactions(Transaction parent) {
    if (parent.children().length == 0)
      return DataUtil.emptyList();
    List<Transaction> trs = arrayList();
    for (var timestamp : parent.children()) {
      var tr = storage().transactionWhichShouldExist(timestamp);
      if (tr == null)
        continue;
      trs.add(tr);
    }
    return trs;
  }

  private void removeChildFromParent(Transaction child) {
    checkArgument(child.parent() != 0, "not a child!");
    var parent = storage().transaction(child.parent());
    if (parent == null) {
      alert("removeChildFromParent; parent not found for:", INDENT, child);
      return;
    }
    var b = parent.toBuilder();
    var ch = LongArray.with(b.children()).toBuilder();
    int j = ch.indexOf(child.timestamp());
    if (j < 0) {
      alert("parent doesn't have reference to child:", parent, CR, child);
      return;
    }
    ch.remove(j);
    b.children(ch.array());
    storage().replaceTransactionWithoutUpdatingAccountBalances(b);
  }

  private File mFile;
  private Rules mRules;
  private Map<Integer, List<Long>> mAccountLedgerCache = hashMap();
  //  private Map<Long, List<Long>> mChildTransactionListCache = hashMap();
  private Account mParentAccount;
  private Transaction mParent;
  private List<Transaction> mNewChildren = arrayList();
}
