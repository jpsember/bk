package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;
import java.util.Stack;

import js.base.BaseObject;
import js.base.Pair;
import js.geometry.MyMath;

public class WinMgr extends BaseObject {

  public static final WinMgr SHARED_INSTANCE;
  private static final int S_TYPE_CONTAINER = 1;

  private WinMgr() {
  }

  public WinMgr pushContainer(JWindow container) {
    checkNotNull(container, "expected container");

    // If this is not going to be the top-level window, add it as a child to the current parent.

    if (!mStack.isEmpty()) {
      var parent = container();
      parent.children().add(container);
    }

    applyParam(container);
    push(S_TYPE_CONTAINER, container);
    return this;
  }

  public WinMgr pushContainer() {
    var container = new JContainer();
    container.mHorzFlag = mHorzFlag;
    mHorzFlag = false;
    return pushContainer(container);
  }

  public WinMgr popContainer() {
    pop(S_TYPE_CONTAINER);
    return this;
  }

  private void push(int type, Object object) {
    checkState(mStack.size() < 100, "stack is too large");
    mStack.push(pair(type, object));
  }

  public WinMgr horz() {
    mHorzFlag = true;
    return this;
  }

  public WinMgr chars(int charCount) {
    checkArgument(charCount > 0, "expected positive character count");
    mSizeExpr = charCount;
    return this;
  }

  public WinMgr pct(int pct) {
    checkArgument(pct > 0, "expected positive percentage");
    mSizeExpr = -pct;
    return this;
  }

  public WinMgr roundedBorder() {
    mBorderType = BORDER_ROUNDED;
    return this;
  }

  public WinMgr thickBorder() {
    mBorderType = BORDER_THICK;
    return this;
  }

  public WinMgr thinBorder() {
    mBorderType = BORDER_THIN;
    return this;
  }

  //  public WinMgr handler(WindowHandler handler) {
  //    mHandler = handler;
  //    return this;
  //  }

  private <T> T pop(int type) {
    if (mStack.size() <= 1)
      badState("attempt to pop the outermost container");
    var x = (T) peek(type);
    mStack.pop();
    return x;
  }

  private <T> T peek(int type) {
    checkState(!mStack.isEmpty(), "stack is empty");
    var p = mStack.peek();
    checkState(p.first == type, "expected stack top to contain", type, "but got", p.first);
    return (T) p.second;
  }

  private JContainer container() {
    return peek(S_TYPE_CONTAINER);
  }

  /**
   * Construct a window and add it to the current container
   */
  public WinMgr window() {
    return window(new JWindow());
  }

  /**
   * Add a window to the current container
   */
  public WinMgr window(JWindow window) {
    checkArgument(window != null, "no window supplied");
    var c = container();
    c.children().add(window);
    applyParam(window);
    return this;
  }

  private void applyParam(JWindow w) {
    w.setSize(mSizeExpr);
    w.setBorder(mBorderType);
    resetPendingWindowVars();
  }

  private void resetPendingWindowVars() {
    mHorzFlag = false;
    mSizeExpr = -100;
    mBorderType = BORDER_NONE;
  }

  public JContainer topLevelContainer() {
    checkState(mStack.size() == 1, "unexpected stack size:", mStack.size());
    return peek(S_TYPE_CONTAINER);
  }

  public FocusHandler focus() {
    return mFocus;
  }

  public void chooseFocus() {
    var c = topLevelContainer();
    openFocusList();
    auxAddFoc(c);
    closeFocusList();
  }

  private void auxAddFoc(JWindow w) {
    addFocus(w);
    for (var c2 : w.children())
      auxAddFoc(c2);
  }

  public void openFocusList() {
    mFocusList = arrayList();
  }

  public void closeFocusList() {
    var f = mFocus;
    if (f != null) {
      if (!mFocusList.contains(f)) {
        f = null;
      }
    }
    if (f == null && !mFocusList.isEmpty())
      f = mFocusList.get(0);
    setFocus(f);
  }

  public void addFocus(JWindow w) {
    if (!(w instanceof FocusHandler))
      return;
    mFocusList.add((FocusHandler) w);
  }

  private List<FocusHandler> mFocusList;

  public void setFocus(FocusHandler h) {
    h = nullTo(h, FOCUS_NONE);
    if (h == mFocus)
      return;
    if (mFocus != null) {
      mFocus.loseFocus();
      mFocus.repaint();
    }
    mFocus = h;
    h.gainFocus();
    h.repaint();
  }

  public void moveFocus(int amount) {
    int slot = mFocusList.indexOf(mFocus);
    switch (amount) {
    case -1:
    case 1:
      slot = MyMath.myMod(slot + amount, mFocusList.size());
      break;
    default:
      badArg("unhandled moveFocus arg", amount);
    }
    setFocus(mFocusList.get(slot));
  }

  private FocusHandler mFocus = FOCUS_NONE;
  private Stack<Pair<Integer, Object>> mStack = new Stack();
  private boolean mHorzFlag;
  private int mBorderType;
  private int mSizeExpr; // 0: unknown > 1: number of chars < 1: -percentage

  public void doneConstruction() {
    // Ensure that only the root container remains on the stack
    if (mStack.size() != 1 || mStack.peek().first != S_TYPE_CONTAINER)
      badState("window stack size is unexpected:", mStack.size(),
          "or doesn't have top-level container at bottom");
  }

  public static final FocusHandler FOCUS_NONE = new FocusHandler() {
  };
  static {
    SHARED_INSTANCE = new WinMgr();
  }
}
