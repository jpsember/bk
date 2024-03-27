package bk;

import java.util.Stack;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.base.Pair;

public class WinMgr extends BaseObject {

  public static final WinMgr SHARED_INSTANCE;
  private static final int S_TYPE_CONTAINER = 1;

  private WinMgr() {
    pushContainer();
  }

  public WinMgr pushContainer() {
    var con = new JContainer();
    con.mHorzFlag = mHorzFlag;
    mHorzFlag = false;
    push(S_TYPE_CONTAINER, con);
    return this;
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
    checkArgument(pct < 0, "expected negative percentage");
    mSizeExpr = pct;
    return this;
  }

  private <T> T pop(int type) {
    if (mStack.size() <= 1)
      badState("attempt to pop the outermost container");
    var x = (T) peek(type);
    mStack.pop();

    if (type == S_TYPE_CONTAINER) {
      todo("layout the container");
    }
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
    w.setSize(mSizeExpr);
    mSizeExpr = 0;
    c.children().add(w);
    return this;
  }

  private Stack<Pair<Integer, Object>> mStack = new Stack();

  static {
    SHARED_INSTANCE = new WinMgr();
  }

  public JContainer topLevelContainer() {
    checkState(mStack.size() == 1, "unexpected stack size:", mStack.size());
    return peek(S_TYPE_CONTAINER);
  }

  private boolean mHorzFlag;
  private int mSizeExpr; // 0: unknown > 1: number of chars < 1: -percentage
}
