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
      t = applyRules(t);
    }
  }

  public Transaction applyRules(Transaction t) {
    if (mDisabled)
      return t;
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
        mTriggerAccountNumber = mParent.credit();
      } else
        continue;
      applyRule(rule);
    }
  }

  private void applyRule(Rule rule) {
    switch (rule.action()) {
    case TRANSFER:
      performTransfer(rule);
      break;
    default:
      badArg("Unsupported rule:", rule);
      break;
    }
  }

  private void disableRules() {
    if (!mDisabled) {
      alert("disabling rules due to problems");
      mDisabled = true;
    }
  }

  private void performTransfer(Rule rule) {

    var parent = mParent;

    int otherAccountNum = rule.targetAccount();

    if (alertAccountDoesNotExist(otherAccountNum, "RuleManager:applyTransferRule")) {
      alert("Rule:", INDENT, rule);
      disableRules();
      return;
    }

    var amount = determineTransactionAmount(parent, rule);

    int dr, cr;
    dr = cr = otherAccountNum;
    int accInd = debitOrCreditIndex(parent, mTriggerAccountNumber);
    if (accInd == 0)
      cr = mTriggerAccountNumber;
    else
      dr = mTriggerAccountNumber;

    if (cr == dr) {
      alert("Transfer DR, CR account numbers are equal:", cr, "; Rule:", INDENT, rule);
      disableRules();
      return;
    }

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
  private double mTransferPercent;
  private String mTransferPercentDesc;
  private boolean mDisabled;
}
