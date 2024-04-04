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
    alertVerbose();
  }

  public FocusHandler focus() {
    return mFocus;
  }

  void update() {
    if (focus() != null)
      return;
    var lst = handlers(null);
    if (lst.isEmpty())
      return;
    set(lst.get(0));
  }

  public void set(FocusHandler h) {
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
    if (h != null) {
      h.gainFocus();
      h.repaint();
    }
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

  private FocusHandler mFocus;

  static {
    SHARED_INSTANCE = new FocusManager();
  }

  private static final int METHOD_APPEND = 0, METHOD_REPLACE = 1;

  /**
   * Push focus on stack, append window to top level container, and make it the
   * new focus
   */
  public void pushAppend(JWindow window) {
    push(window, METHOD_APPEND);
  }

  /**
   * Push focus on stack, replace top level container's contents with this new
   * window, and make it the focus
   */
  public void pushReplace(JWindow window) {
    push(window, METHOD_REPLACE);
  }

  private void push(JWindow window, int method) {
    log("push", window.name(), "method:", method);

    // If window is already in the stack somewhere, pop to it
    int popTo = -1;
    for (int i = mStack.size() - 1; i >= 0; i--) {
      var ent = mStack.get(i);
      if (ent.oldFocusHandler == window) {
        log("...found window in stack at:", i);
        popTo = i;
      }
    }
    if (popTo >= 0) {
      log("...popping stack to size", popTo, "to restore stacked focus");
      while (mStack.size() > popTo) {
        pop();
      }
      return;
    }

    var mgr = winMgr();
    checkNotNull(window);
    var top = mgr.topLevelContainer();
    checkState(top != window);

    var ent = new StackEntry(method, mFocus);
    ent.windows.addAll(top.children());

    // If window is already in the hierarchy, make sure it's parent is the top level container
    if (mgr.inView(window)) {
      checkState(window.parent() == top, "window is not within top level container, instead it's within",
          window.parent().name());
    } else {
      switch (method) {
      default:
        notSupported("push method");
        break;
      case METHOD_REPLACE:
        top.removeChildren();
        top.addChild(window);
        break;
      case METHOD_APPEND:
        top.addChild(window);
        break;
      }
    }
    mStack.push(ent);
    log("...pushed entry to stack, size now:", mStack.size());
    trySettingFocus(window);
  }

  private void trySettingFocus(JWindow window) {
    FocusHandler newHandler = null;

    if (window instanceof FocusHandler) {
      newHandler = (FocusHandler) window;
    } else {
      // See which children are focus handlers
      var childHandlers = handlers(window);
      if (!childHandlers.isEmpty())
        newHandler = childHandlers.get(0);
    }
    if (newHandler == null) {
      alert("window has no FocusHandlers:", window);
    } else
      set(newHandler);
  }

  public void pop() {
    todo("intercept attempt to leave form?");
    log("pop; stack size:", mStack.size());
    if (mStack.isEmpty()) {
      badState("FocusHandler stack is empty");
    }

    var mgr = winMgr();
    var ent = mStack.pop();
    var top = mgr.topLevelContainer();
    top.removeChildren();
    for (var child : ent.windows) {
      top.addChild(child);
    }

    set(ent.oldFocusHandler);
  }

  private static class StackEntry {
    StackEntry(int method, FocusHandler handlerToSave) {
      //this.method = method;
      this.windows = arrayList();
      this.oldFocusHandler = handlerToSave;
    }

    FocusHandler oldFocusHandler;
    //int method;
    List<JWindow> windows;
  }

  public boolean popIfPossible() {
    // Don't pop the last container
    if (mStack.size() == 0)
      return false;
    pop();
    return true;
  }

  private Stack<StackEntry> mStack = new Stack<>();

}
