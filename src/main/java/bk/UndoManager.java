package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import bk.gen.Account;
import bk.gen.Transaction;
import bk.gen.UndoAction;
import bk.gen.UndoEntry;
import js.base.BaseObject;
import js.base.BasePrinter;

public class UndoManager extends BaseObject {

  public static final UndoManager SHARED_INSTANCE = new UndoManager();

  private UndoManager() {
    alertVerbose();
  }

  public void begin(Object... description) {
    var msg = BasePrinter.toString(description);
    log("begin:", msg);
    assertState(STATE_DORMANT);
    setState(STATE_EXECUTING);
    mAction = UndoAction.newBuilder().description(msg);
  }

  public void end() {
    assertState(STATE_EXECUTING);
    var act = mAction.build();
    mAction = null;
    removeAllButFirstN(mStack, mStackPointer);
    mStack.add(act);
    mStackPointer++;
    log(VERT_SP, "added to undo stack:", INDENT, act);
  }

  private int assertState(int expected) {
    if (mState != expected) {
      badState("expected state", stateName(expected), "but found", stateName(mState));
    }
    return expected;
  }

  private int setState(int s) {
    if (mState != s) {
      log("state changing to:", stateName(s));
      mState = s;
    }
    return s;
  }

  private static String[] sStateNames = { "DORMANT", "EXECUTING", "UNDOING", };

  private String stateName(int state) {
    return sStateNames[state];
  }

  private static final int STATE_DORMANT = 0, STATE_EXECUTING = 1, STATE_UNDOING = 2, STATE_REDOING = 3, STATE_TOTAL = 4;

  private int mState = STATE_DORMANT;

  private UndoAction.Builder mAction;

  public boolean recording() {
    return mState == STATE_EXECUTING;
  }

  public void addAccount(Account a) {
    if (!recording())
      return;
    mAction.entries().add(UndoEntry.newBuilder().account(a).insert(true));
  }

  public void deleteAccount(Account a) {
    if (!recording())
      return;
    mAction.entries().add(UndoEntry.newBuilder().account(a).insert(false));
  }

  public void addTransaction(Transaction t) {
    if (!recording())
      return;
    mAction.entries().add(UndoEntry.newBuilder().transaction(t).insert(true));
  }

  public void deleteTransaction(Transaction t) {
    if (!recording())
      return;
    mAction.entries().add(UndoEntry.newBuilder().transaction(t).insert(false));
  }

  public boolean performUndo() {
    if (mStackPointer == 0) {
      log("can't undo anything");
      return false;
    }
    mStackPointer--;
    var evt = mStack.get(mStackPointer);

    log(VERT_SP, "undoing:", INDENT, evt);
    setState(STATE_UNDOING);
    for (int j = evt.entries().size() - 1; j >= 0; j--) {
      var ent = evt.entries().get(j);
      undo(ent);
    }
    setState(STATE_DORMANT);
    return true;
  }

  private void undo(UndoEntry ent) {
    log("undoing entry:", INDENT, ent);
    var s = storage();
    if (ent.insert()) {
      if (ent.account() != null)
        s.deleteAccount(ent.account().number());
      else
        s.deleteTransaction( ent.transaction() );
    } else {
      if (ent.account() != null)
        s.addOrReplace(ent.account());
      else
        s.addOrReplace(ent.transaction());
    }
  }

  private List<UndoAction> mStack = arrayList();
  private int mStackPointer;

  public boolean live() {
    return mState != STATE_UNDOING && mState != STATE_REDOING;
  }
}
