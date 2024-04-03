package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;
import java.util.Stack;

import js.base.BaseObject;
import js.geometry.MyMath;

public class FocusManager extends BaseObject {

  public static final FocusManager SHARED_INSTANCE;

  private FocusManager() {
  }

  public FocusHandler focus() {
    return mFocus;
  }

  void update() {
    if (focus() != FOCUS_NONE)
      return;
    var lst = handlers(null);
    if (lst.isEmpty())
      return;
    set(lst.get(0));
  }

  public void set(FocusHandler h) {
    h = nullTo(h, FOCUS_NONE);
    if (h == mFocus)
      return;
    if (mFocus != null) {
      winMgr().hideCursor();
      mFocus.loseFocus();
      mFocus.repaint();
    }
    if (h != null && h instanceof JWindow)
      checkArgument(((JWindow) h).parent() != null, "attempt to focus a window that isn't visible:", h);
    mFocus = h;
    h.gainFocus();
    h.repaint();
  }

  public void move(JWindow rootWindowOrNull, int amount) {
    var focusList = handlers(rootWindowOrNull);
    int slot = focusList.indexOf(mFocus);
    switch (amount) {
    case -1:
    case 1:
      slot = MyMath.myMod(slot + amount, focusList.size());
      break;
    default:
      badArg("unhandled moveFocus arg", amount);
    }
    set(focusList.get(slot));
  }

  public final List<FocusHandler> handlers(JWindow topLevelWindowOrNull) {
    var w = nullTo(topLevelWindowOrNull, winMgr().topLevelContainer());
    List<FocusHandler> out = arrayList();
    auxFocusList(out, w);
    return out;
  }

  private void auxFocusList(List<FocusHandler> list, JWindow window) {
    if (window.hidden())
      return;
    if (window instanceof FocusHandler) {
      list.add((FocusHandler) window);
    }

    for (var c : window.children())
      auxFocusList(list, c);
  }

  private FocusHandler mFocus = FOCUS_NONE;

  public static final FocusHandler FOCUS_NONE = new FocusHandler() {
  };

  static {
    SHARED_INSTANCE = new FocusManager();
  }

  public void push(FocusHandler focus) {
    checkNotNull(focus);
    checkArgument(focus != FOCUS_NONE, "attempt to push FOCUS_NONE");
    if (mFocus != FOCUS_NONE)
      mStack.push(mFocus);

    JWindow w = (JWindow) focus;
    if (w.parent() == null) {
      switchToView(w);
    }
    set(focus);
  }

  public void pop() {
    FocusHandler r = null;
    if (mStack.isEmpty()) {
      alert("FocusHandler stack is empty");

      var focusList = handlers(null);
      if (focusList.isEmpty()) {
        alert("attempt to restore focus, but none are available");
        return;
      }
      if (focusList.size() > 1) {
        pr("multiple focus handlers to choose from!");
      }
      r = focusList.get(0);
    } else {
      r = mStack.pop();
    }

    var f = (FocusHandler) r;

    // JContainer c = winMgr().topLevelContainer();
    JWindow w = (JWindow) f;
    if (w.parent() == null) {
      switchToView(w);
      //      // Replace the top-level contents with this window
      //      c.removeChildren();
      //      w.mSizeExpr = 100;
      //      c.addChild(w);
    } else
      set(f);
  }

  private Stack<FocusHandler> mStack = new Stack<>();

  public boolean popIfPossible() {
    if (mStack.isEmpty())
      return false;
    pop();
    return true;
  }
}
