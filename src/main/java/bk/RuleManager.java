package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;

import bk.gen.Transaction;
import bk.gen.rules.ActionName;
import bk.gen.rules.Rule;
import bk.gen.rules.Rules;
import js.base.BaseObject;
import js.data.LongArray;
import js.file.Files;

public class RuleManager extends BaseObject {

  public static final RuleManager SHARED_INSTANCE = new RuleManager();

  private RuleManager() {
    //alertVerbose();
  }

  public void deleteAllGeneratedTransactions() {
    var allTrans = storage().readAllTransactions();
    for (var t : allTrans) {
      if (t.parent() != 0) {
        storage().deleteTransaction(t);
      } else {
        if (t.children().length != 0) {
          t = t.toBuilder().children(null).build();
          storage().replaceTransactionWithoutUpdatingAccountBalances(t);
        }
      }
    }
  }

  public void applyRulesToAllTransactions() {
    deleteAllGeneratedTransactions();
    var allTrans = storage().readAllTransactions();
    for (var t : allTrans) {
      applyRules(t);
    }

    // Apply monthly summary rule as well (if one exists)

    for (var entry : rules().rules().entrySet()) {
      if (mDisabled)
        return;
      var rule = entry.getValue();
      if (rule.action() != ActionName.MONTHLY_SUMMARY)
        continue;
      applyMonthlySummaryRule(rule);
    }

  }

  public Transaction applyRules(Transaction t) {
    if (mDisabled)
      return t;
    // If this is a generated transaction, don't apply any rules
    if (t.parent() != 0)
      return t;

    // If this transaction involves year end closing, don't apply any rules
    if (isClosingAcct(t.debit()) || isClosingAcct(t.credit()))
      return t;

    checkState(t.children().length == 0, "attempt to apply rules to a transaction that already has children");

    mParent = t;
    mNewChildren.clear();
    mTriggerAccountNumber = 0;
    applyRules();

    if (!mNewChildren.isEmpty()) {
      log("...we have new children for:", mParent.timestamp(), INDENT, mNewChildren);

      // Add these children to the existing list of children

      var b = mParent.toBuilder();
      var bchild = LongArray.with(b.children()).toBuilder();
      for (var ch : mNewChildren) {
        storage().addOrReplace(ch);
        changeManager().registerModifiedTransactions(ch);
        bchild.add(ch.timestamp());
      }
      b.children(bchild.array());
      log("replacing transaction:", INDENT, b);
      storage().replaceTransactionWithoutUpdatingAccountBalances(b);
      t = b.build();
    }
    return t;
  }

  private boolean isClosingAcct(int anum) {
    return anum == ACCT_EQUITY || anum == ACCT_INCOME_SUMMARY;
  }

  private static final boolean intWithinArray(int[] array, int value) {
    for (int j = array.length - 1; j >= 0; j--)
      if (array[j] == value)
        return true;
    return false;
  }

  /**
   * Apply any rules to the current transaction.
   * 
   * Actually, only the TRANSFER rule gets applied here.
   */
  private void applyRules() {
    for (var entry : rules().rules().entrySet()) {
      var rule = entry.getValue();

      if (!withinDateRange(mParent, rule))
        continue;

      if (rule.action() != ActionName.TRANSFER)
        continue;
      if (intWithinArray(rule.accounts(), mParent.debit())) {
        mTriggerAccountNumber = mParent.debit();
      } else if (intWithinArray(rule.accounts(), mParent.credit())) {
        mTriggerAccountNumber = mParent.credit();
      } else
        continue;
      applyRule(rule);
    }
  }

  private boolean withinDateRange(Transaction t, Rule rule) {
    if (rule.parsedDateMin() > 0 && t.date() < rule.parsedDateMin())
      return false;
    if (rule.parsedDateMax() > 0 && t.date() > rule.parsedDateMax())
      return false;
    return true;
  }

  private void applyRule(Rule rule) {
    pr("............. applying rule:", INDENT, rule);
    switch (rule.action()) {
    case TRANSFER:
      performTransfer(rule);
      break;
    default:
      badArg("Unsupported rule:", rule);
      break;
    }
  }

  private List<Integer> constructAccountList(int[] accounts) {
    Set<Integer> s = hashSet();

    var accList = storage().readAllAccounts();
    List<Integer> anums = arrayList();
    for (var acct : accList) {
      anums.add(acct.number());
    }
    anums.sort(null);

    for (int x : accounts) {
      if (x < 0) {
        int astart = (-x / 1000) * 1000;
        int aend = astart + 999;
        for (int an : anums)
          if (an >= astart && an <= aend)
            s.add(an);
      } else if (anums.contains(x))
        s.add(x);
    }

    List<Integer> result = arrayList();
    result.addAll(s);
    result.sort(null);
    return result;
  }

  private void applyMonthlySummaryRule(Rule rule) {
    var accts = constructAccountList(rule.accounts());
    for (int pass = 0; pass < 2; pass++) {
      int anum = (pass == 0) ? rule.sourceAccount() : rule.targetAccount();
      if (storage().account(anum) == null) {
        disableRulesDueToProblem("rule source/target doesn't exist:", anum, INDENT, rule);
        return;
      }
    }

    // Construct a map of months -> total
    Map<Long, Long> monthAmountMap = hashMap();
    for (int sourceAccountNum : accts) {
      var trs = storage().readTransactionsForAccount(sourceAccountNum);
      for (var t : trs) {
        // Determine start of month containing this transaction
        var s = formatDate(t.date());
        // Trim off the day within month
        s = s.substring(0, s.length() - 2);
        s = s + "01";
        var epochSec = dateToEpochSeconds(s);
        Long currency = monthAmountMap.getOrDefault(epochSec, 0L);
        int sign = (t.debit() == sourceAccountNum) ? 1 : -1;
        currency += sign * t.amount();
        monthAmountMap.put(epochSec, currency);
      }
    }

    final String DESC = "(generated)";
    // Delete any existing transactions generated by this rule
    for (int pass = 0; pass < 2; pass++) {
      int anum = (pass == 0) ? rule.sourceAccount() : rule.targetAccount();
      var trs = storage().readTransactionsForAccount(anum);
      for (var t : trs) {
        if (t.description().equals(DESC)) {
          changeManager().registerModifiedTransactions(t);
          storage().deleteTransaction(t);
        }
      }
    }

    for (var ent : monthAmountMap.entrySet()) {
      var tr = newTransactionBuilder();
      var val = ent.getValue();
      tr.date(ent.getKey());
      if (val >= 0) {
        tr.debit(rule.sourceAccount());
        tr.credit(rule.targetAccount());
      } else {
        tr.credit(rule.sourceAccount());
        tr.debit(rule.targetAccount());
      }
      tr.amount(Math.abs(val));
      tr.description(DESC);

      var t = tr.build();
      changeManager().registerModifiedTransactions(t);
      storage().addOrReplace(t);
    }
  }

  private void disableRulesDueToProblem(Object... message) {
    if (!mDisabled) {
      alert("disabling rules due to problems!", message);
      mDisabled = true;
    }
  }

  private void performTransfer(Rule rule) {

    var parent = mParent;

    int otherAccountNum = rule.targetAccount();

    if (alertAccountDoesNotExist(otherAccountNum, "RuleManager:applyTransferRule")) {
      disableRulesDueToProblem("account doesn't exist:", otherAccountNum, "for rule:", INDENT, rule);
      return;
    }

    var amount = determineTransactionAmount(parent, rule);

    int dr, cr;
    dr = cr = otherAccountNum;

    int accInd = debitOrCreditIndex(parent, mTriggerAccountNumber);
    if (accInd < 0) {
      disableRulesDueToProblem("trigger account was not debit or credit", INDENT, rule);
      return;
    }

    if (accInd == 0)
      cr = mTriggerAccountNumber;
    else
      dr = mTriggerAccountNumber;

    if (cr == dr) {
      disableRulesDueToProblem("Transfer DR,CR numbers equal!", INDENT, rule);
      return;
    }

    // If there is already a generated transaction in the child list matching this one, do nothing
    var existing = findChildTransaction(dr, cr, amount);
    if (existing != null) {
      log("...a child transaction already exists for dr:", dr, "cr:", cr, "amount:", amount, INDENT,
          existing);
      return;
    }

    var tr = newTransactionBuilder();
    tr.date(parent.date());
    tr.amount(amount);
    tr.debit(dr);
    tr.credit(cr);
    tr.description(mTransferPercentDesc + " of " + formatCurrency(parent.amount()));
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

  private long determineTransactionAmount(Transaction parentTransaction, Rule rule) {
    double pct = rule.percent();
    if (pct == 0) {
      badArg("percentage is zero (or missing):", INDENT, rule);
    }
    mTransferPercent = pct;
    mTransferPercentDesc = chomp(String.format("%.2f", pct), ".00") + "%";
    var amountInCents = Math.round(parentTransaction.amount() * (mTransferPercent / 100));
    return amountInCents;
  }

  private static final String EXPECTED_VERSION = "2.0";

  private Rules rules() {
    if (mRules == null) {
      var r = Files.parseAbstractDataOpt(Rules.DEFAULT_INSTANCE, file());
      r = updateRules(r);
      r = parseDates(r);
      mRules = r;
      log("read rules:", INDENT, mRules);
      // Reformat them, and save (with backup) if it has changed
      var str = mRules.toString();
      if (!file().exists())
        Files.S.writeString(file(), "{}");
      var content = Files.readString(file());
      if (!content.equals(str)) {
        log("...rules changed with formatting from:", INDENT, content);
        log("to:", INDENT, str);
        Files.S.copyFile(file(), Files.getDesktopFile("_rules_backup_.json"));
        Files.S.writeString(file(), str);
      }
    }
    return mRules;
  }

  private Rules updateRules(Rules r) {
    var version = r.version();
    if (version.equals(EXPECTED_VERSION))
      return r;

    var b = r.toBuilder();
    if (!(version.equals("") && EXPECTED_VERSION == "2.0")) {
      badState("Rules have an unsupported version:", version);
    }
    b.version(EXPECTED_VERSION);
    r = b.build();
    return r;
  }

  private Rules parseDates(Rules r0) {
    Rules.Builder b = r0.toBuilder();
    Map<String, Rule> newMap = hashMap();
    for (var ent : b.rules().entrySet()) {
      var rb = ent.getValue().toBuilder();
      var t0 = parseDate(rb.dateMin());
      var t1 = parseDate(rb.dateMax());
      rb.parsedDateMin(t0);
      rb.parsedDateMax(t1);
      newMap.put(ent.getKey(), rb.build());
    }

    b.rules(newMap);
    return b.build();
  }

  private long parseDate(String dateExpr) {
    if (nullOrEmpty(dateExpr))
      return 0;
    var res = DATE_VALIDATOR.validate(dateExpr);
    long sec = res.typedValue();
    return sec;
  }

  private File file() {
    return storage().rulesFile();
  }

  private Rules mRules;
  private Transaction mParent;
  private int mTriggerAccountNumber;
  private List<Transaction> mNewChildren = arrayList();
  private double mTransferPercent;
  private String mTransferPercentDesc;
  private boolean mDisabled;
}
