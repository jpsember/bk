package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;
import java.util.Map;

import bk.gen.Account;
import bk.gen.Transaction;
import js.base.BaseObject;

public class ChangeManager extends BaseObject {

  public ChangeManager() {
    //alertVerbose();
  }

  public ChangeManager addListener(ChangeListener listener) {
    checkNotNull(listener);
    checkState(!mListeners.contains(listener), "listener added twice");
    mListeners.add(listener);
    return this;
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
    mark("modify change manager to handle rules");

    if (mTr.isEmpty() && mAc.isEmpty())
      return;

    log("====== dispatching changes");

    List<Integer> aIds = arrayList();
    List<Long> tIds = arrayList();
    aIds.addAll(mAc.keySet());
    tIds.addAll(mTr.keySet());

    for (var x : mListeners) {
      x.dataChanged(aIds, tIds);
    }

    mTr.clear();
    mAc.clear();
    log("==== done dispatch", VERT_SP);
  }

  private Map<Long, Transaction> mTr = hashMap();
  private Map<Integer, Account> mAc = hashMap();
  private List<ChangeListener> mListeners = arrayList();
}
