package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.io.File;
import java.util.List;

import bk.gen.Transaction;
import bk.gen.rules.Rule;
import bk.gen.rules.Rules;
import js.base.BaseObject;
import js.data.LongArray;
import js.file.Files;
import js.json.JSMap;

public class RuleManager extends BaseObject {

  public static final RuleManager SHARED_INSTANCE = new RuleManager();

  private RuleManager() {
    todo("!don't allow editing of generated transactions");
   // alertVerbose();
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
      t = applyRules(t);
    }
  }

  public Transaction applyRules(Transaction t) {
    // If this is a generated transaction, don't apply any rules
    if (t.parent() != 0)
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

  private static final boolean intWithinArray(int[] array, int value) {
    for (int j = array.length - 1; j >= 0; j--)
      if (array[j] == value)
        return true;
    return false;
  }

  /**
   * Apply any rules to the current transaction
   */
  private void applyRules() {
    for (var entry : rules().rules().entrySet()) {
      var rule = entry.getValue();
      if (intWithinArray(rule.accounts(), mParent.debit())) {
        mTriggerAccountNumber = mParent.debit();
      } else if (intWithinArray(rule.accounts(), mParent.credit())) {
        mTriggerAccountNumber = mParent.debit();
      } else
        continue;
      applyRule(rule);
    }
  }

  private void applyRule(Rule rule) {
    if (!rule.conditions().isEmpty())
      alert("ignoring rule conditions for now:", INDENT, rule);

    for (var actionMap : rule.actions()) {
      String actionName = actionMap.opt("action", "transfer");

      switch (actionName) {
      default:
        alert("unsupported action:", actionMap);
        break;
      case "transfer":
        applyTransferRule(actionMap);
        break;
      }
    }

  }

  private void applyTransferRule(JSMap actionMap) {

    var parent = mParent;
    int otherAccountNum = actionMap.getInt("account");

    var amount = determineTransactionAmount(parent, actionMap, "amount");
    int dr, cr;
    dr = cr = otherAccountNum;
    if (parent.debit() == mTriggerAccountNumber) {
      cr = mTriggerAccountNumber;
    } else if (parent.credit() == mTriggerAccountNumber) {
      dr = mTriggerAccountNumber;
    } else
      throw badState("expected transaction to reference trigger acct #:", mTriggerAccountNumber, parent);

    todo("!why are generated transactions appearing before their parents in the account ledger?");

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
    var lst = storage().getChildTransactions(parent);
    for (var t : lst) {
      if (t.debit() == dr && t.credit() == cr && t.amount() == amount) {
        return t;
      }
    }
    return null;
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
      // Reformat them, and save (with backup) if it has changed
      var str = mRules.toString();
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

  private File file() {
    if (mFile == null) {
      var sf = storage().file();
      mFile = new File(Files.removeExtension(sf) + ".rules.json");
      if (verbose())
        log("set file to:", INDENT, Files.infoMap(mFile));
    }
    return mFile;
  }

  private File mFile;
  private Rules mRules;
  private Transaction mParent;
  private int mTriggerAccountNumber;
  private List<Transaction> mNewChildren = arrayList();

}
