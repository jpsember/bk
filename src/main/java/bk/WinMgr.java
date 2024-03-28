package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.Map;
import java.util.Stack;

import js.base.BaseObject;
import js.base.Pair;

public class WinMgr extends BaseObject {

  public static final WinMgr SHARED_INSTANCE;
  private static final int S_TYPE_CONTAINER = 1;

  private WinMgr() {
  }

  public JContainer rootContainer() {
    if (mRootContainer == null)
      throw badState("no root container defined");
    return mRootContainer;
  }

  public WinMgr pushContainer() {
    var container = new JContainer();

    container.mHorzFlag = mHorzFlag;
    mHorzFlag = false;

    // If this is not going to be the top-level window, add it as a child to the current parent.

    if (mRootContainer == null) {
      mRootContainer = container;
    } else {
      var parent = container();
      parent.children().add(container);
    }

    applyParam(container);
    push(S_TYPE_CONTAINER, container);
    return this;
  }

  public WinMgr popContainer() {
    pop(S_TYPE_CONTAINER);
    return this;
  }

  public WinMgr id(int nextId) {
    mPendingId = nextId;
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

  public WinMgr handler(WindowHandler handler) {
    mHandler = handler;
    return this;
  }

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
    var c = container();
    var w = new JWindow();
    c.children().add(w);
    applyParam(w);
    checkState(!mWindowMap.containsKey(w.id()));
    mWindowMap.put(w.id(), w);
    return this;
  }

  private void applyParam(JWindow w) {
    w.setSize(mSizeExpr);
    w.setBorder(mBorderType);
    w.setHandler(mHandler);
    if (mPendingId == 0) {
      mPendingId = mUniqueId++;
    }
    w.setId(mPendingId);
    resetPendingWindowVars();
  }

  private void resetPendingWindowVars() {
    mHorzFlag = false;
    mSizeExpr = -100;
    mBorderType = BORDER_NONE;
    mHandler = null;
    mPendingId = 0;
  }

  public JContainer topLevelContainer() {
    checkState(mStack.size() == 1, "unexpected stack size:", mStack.size());
    return peek(S_TYPE_CONTAINER);
  }

  public JWindow focusWindow() {
    return mFocusWindow;
  }

  public JWindow get(int id) {
    var w = mWindowMap.get(id);
    if (w == null)
      badArg("no window found with id:", id);
    return w;
  }

  public void setFocusWindow(JWindow window) {
    if (window == mFocusWindow)
      return;
    // If focus window is changing, repaint both old and new, in case we're highlighting things to 
    // emphasize the focus
    if (mFocusWindow != null)
      mFocusWindow.repaint();
    window.repaint();
    mFocusWindow = window;
  }

  private JWindow mFocusWindow;

  private Stack<Pair<Integer, Object>> mStack = new Stack();
  private boolean mHorzFlag;
  private int mBorderType;
  private int mSizeExpr; // 0: unknown > 1: number of chars < 1: -percentage
  private WindowHandler mHandler;
  private JContainer mRootContainer;
  private int mPendingId;
  private int mUniqueId = -10000;
  private Map<Integer, JWindow> mWindowMap = hashMap();

  public void doneConstruction() {
    // Ensure that only the root container remains on the stack
    if (mStack.size() != 1 || mStack.peek().first != S_TYPE_CONTAINER)
      badState("window stack size is unexpected:", mStack.size(),
          "or doesn't have top-level container at bottom");
  }

  static {
    SHARED_INSTANCE = new WinMgr();
  }
}
