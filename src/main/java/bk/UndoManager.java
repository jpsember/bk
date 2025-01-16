package bk;

import static bk.Util.*;
import static js.base.Tools.*;
import static bk.gen.UndoState.*;

import java.util.List;

import bk.gen.Account;
import bk.gen.Transaction;
import bk.gen.UndoAction;
import bk.gen.UndoEntry;
import bk.gen.UndoState;
import js.base.BaseObject;
import js.base.BasePrinter;

public class UndoManager extends BaseObject {

  public static final UndoManager SHARED_INSTANCE = new UndoManager();

  private UndoManager() {
    //alertVerbose();
  }

  public void begin(Object... description) {
    var msg = BasePrinter.toString(description);
    log("begin:", msg);
    assertState(DORMANT);
    setState(RECORDING);
    mAction = UndoAction.newBuilder().description(msg);
  }

  public void end() {
    log("end");
    assertState(RECORDING);
    setState(DORMANT);
    var act = mAction.build();
    mAction = null;
    removeAllButFirstN(mStack, mStackPointer);
    mStack.add(act);
    mStackPointer++;
    log(VERT_SP, "added to undo stack:", INDENT, act);
  }

  private UndoState assertState(UndoState expected) {
    if (mState != expected) {
      badState("expected state", expected, "but found", mState);
    }
    return expected;
  }

  private UndoState setState(UndoState s) {
    if (mState != s) {
      log("state changing to:", s);
      mState = s;
    }
    return s;
  }

  private UndoState mState = UndoState.DORMANT;

  private UndoAction.Builder mAction;

  public boolean recording() {
    return stateIs(UndoState.RECORDING);
  }

  private boolean stateIs(UndoState s) {
    return mState == s;
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
    setState(UNDOING);
    for (int j = evt.entries().size() - 1; j >= 0; j--) {
      var ent = evt.entries().get(j);
      undo(ent);
    }
    setState(DORMANT);
    focusManager().validate();
    return true;
  }

  public boolean performRedo() {
    if (mStackPointer == mStack.size()) {
      log("can't redo anything");
      return false;
    }
    var evt = mStack.get(mStackPointer);
    mStackPointer++;

    log(VERT_SP, "redoing:", INDENT, evt);
    setState(REDOING);
    for (var ent : evt.entries()) {
      undo(ent);
    }
    setState(DORMANT);
    focusManager().validate();
    return true;
  }

  private void undo(UndoEntry ent) {
    boolean redo = mState == REDOING;
    log(redo ? "redoing" : "undoing", "entry:", INDENT, ent);
    var s = storage();
    changeManager().registerModifiedAccount(ent.account());
    changeManager().registerModifiedTransaction(ent.transaction());
    if (ent.insert() ^ redo) {
      if (ent.account() != null)
        s.deleteAccount(ent.account().number());
      else {
        // This transaction may no longer exist in some cases;
        // don't attempt to delete it unless it exists
        var exists = s.transaction(id(ent.transaction()));
        if (exists != null)
          s.deleteTransaction(ent.transaction());
      }
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
    return !stateIs(UNDOING) && !stateIs(REDOING);
  }
}
