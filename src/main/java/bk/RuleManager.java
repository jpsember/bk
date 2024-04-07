package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bk.gen.Account;
import bk.gen.Transaction;
import bk.gen.rules.Rule;
import bk.gen.rules.Rules;
import js.base.BaseObject;
import js.file.Files;
import js.json.JSMap;

public class RuleManager extends BaseObject {

  public static final RuleManager SHARED_INSTANCE = new RuleManager();

  private RuleManager() {
    alertVerbose();
  }

  public void applyRules(Set<Integer> accountIds) {
    loadTools();
    if (accountIds.isEmpty())
      return;
    log("applying rules, accounts:", accountIds);
    for (var id : accountIds) {
      var account = storage().account(id);
      if (account == null) {
        log("...account doesn't exist:", id);
        continue;
      }
      applyRule(account);
    }
  }

  private void applyRule(Account account) {
    for (var entry : rules().rules().entrySet()) {
      var rule = entry.getValue();
      if (!rule.accounts().contains(account.number()))
        continue;
      applyRule(rule, account);
    }

  }

  private void applyRule(Rule rule, Account account) {

    if (!rule.conditions().isEmpty())
      alert("ignoring rule conditions for now:", INDENT, rule);

    for (var actionMap : rule.actions()) {
      String actionName = actionMap.opt("action", "generate");

      switch (actionName) {
      default:
        alert("unsupported action:", actionMap);
        break;
      case "generate":
        applyGenerateRule(account, actionMap);
        break;
      }
    }

  }

  private void applyGenerateRule(Account account, JSMap actionMap) {
    int sourceAccountNum = determineAccountNumber(account, actionMap, "source", account.number());
    int targetAccountNum = determineAccountNumber(account, actionMap, "target", null);
    mark("finish writing this code:");
    // var amount = determineTransactionAmount(actionMap,"amount",
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

  private File mFile;
  private Rules mRules;
  private Map<Integer, List<Transaction>> mAccountLedgerCache = hashMap();
}
