package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import bk.gen.Account;
import bk.gen.Transaction;
import bk.gen.UndoAction;
import bk.gen.UndoEntry;
import js.base.BaseObject;
import js.base.BasePrinter;

public class UndoManager extends BaseObject {

  public static final UndoManager SHARED_INSTANCE = new UndoManager();

  private UndoManager() {
    loadTools();
    loadUtil();
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
    todo("do something with this undo action:", INDENT, act);
    mUndoEvent = act;
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

  private static final int STATE_DORMANT = 0, STATE_EXECUTING = 1, STATE_UNDOING = 2, STATE_TOTAL = 3;

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

  public void deleteTransaction(Transaction t) {
    if (!recording())
      return;
    mAction.entries().add(UndoEntry.newBuilder().transaction(t).insert(false));
  }

  public void addTransaction(Transaction t) {
    if (!recording())
      return;
    mAction.entries().add(UndoEntry.newBuilder().transaction(t).insert(true));
  }

  public boolean performUndo() {
    if (mUndoEvent == null) {
      log("can't undo anything");
      return false;
    }

    pr("not finished; undo:", mUndoEvent.description());
    return true;
  }

  private UndoAction mUndoEvent;

}
