package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;
import java.util.Map;
import java.util.Stack;

import bk.gen.Account;
import bk.gen.Transaction;
import js.base.BaseObject;

public class ChangeManager extends BaseObject {

  public ChangeManager() {
    alertVerbose();
  }

  public ChangeManager registerModifiedTransactions(Transaction... t) {
    for (var tr : t)
      registerModifiedTransaction(tr);
    return this;
  }

  public ChangeManager registerModifiedTransaction(Transaction tr) {
    if (tr != null) {
      var key = id(tr);
      if (verbose() && !mTr.containsKey(key))
        log("=== tr mod:", key);
      mTr.put(key, tr);
      for (int j = 0; j < 2; j++) {
        registerModifiedAccount(account(tr, j));
      }
    }
    return this;
  }

  public ChangeManager registerModifiedAccounts(Account... accounts) {
    for (var a : accounts)
      registerModifiedAccount(a);
    return this;
  }

  public ChangeManager registerModifiedAccount(int accountNumber) {
    return registerModifiedAccount(account(accountNumber));
  }

  public ChangeManager registerModifiedAccount(Account a) {
    if (a != null) {
      var key = id(a);
      if (verbose() && !mAc.containsKey(key))
        log("=== ac mod:", key);
      mAc.put(key, a);
    }
    return this;
  }

  public void dispatch() {
    if (mTr.isEmpty() && mAc.isEmpty())
      return;

    log("====== dispatching changes");

    List<Integer> modifiedAccountNumbers = arrayList();
    List<Long> modifiedTransactionIds = arrayList();
    modifiedAccountNumbers.addAll(mAc.keySet());
    modifiedTransactionIds.addAll(mTr.keySet());
    var mgr = winMgr();
    var stack = new Stack<JWindow>();
    stack.add(mgr.topLevelContainer());
    while (!stack.empty()) {
      var w = stack.pop();
      if (w instanceof ChangeListener) {
        var m = (ChangeListener) w;
        log("....notifying window", w.name());
        m.dataChanged(modifiedAccountNumbers, modifiedTransactionIds);
      }
      for (var c : w.children())
        stack.push(c);
    }

    mTr.clear();
    mAc.clear();
    log("==== done dispatch", VERT_SP);
  }

  private Map<Long, Transaction> mTr = hashMap();
  private Map<Integer, Account> mAc = hashMap();
}
