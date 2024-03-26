package bk;

import java.util.Stack;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.base.Pair;

public class WinMgr extends BaseObject {

  private static final int S_TYPE_CONTAINER = 1;

  public WinMgr pushContainer() {
    var con = new JContainer();
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

  private <T> T pop(int type) {
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
    c.children().add(w);
    return this;
  }

  private Stack<Pair<Integer, Object>> mStack = new Stack();
}
