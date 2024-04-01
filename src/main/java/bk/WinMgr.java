package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.Stack;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.AbstractScreen;

import js.base.BaseObject;
import js.base.Pair;
import js.geometry.IPoint;
import js.geometry.IRect;

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

  public void setCursorPosition(int x, int y) {
    mScreen.setCursorPosition(new TerminalPosition(x, y));
  }

  public void hideCursor() {
    mScreen.setCursorPosition(null);
  }

  public AbstractScreen abstractScreen() {
    return mScreen;
  }

  public void mainLoop() {
    var js = JScreen.sharedInstance();
    while (js.isOpen()) {
      update();
      sleepMs(10);
      if (quitRequested())
        js.close();
    }
  }

  public void update() {
    var m = winMgr();
    try {

      focusManager().update();

      KeyStroke keyStroke = mScreen.pollInput();
      if (keyStroke != null) {
        if (keyStroke.getKeyType() == KeyType.Escape) {
          todo("Have a special key, like ctrl q, to quit");
          quit();
          return;
        }
        //var w = m.focus();
        todo("do we need to prepare handler?");
        focusManager().focus().processKeyStroke(keyStroke);
      }

      var c = m.topLevelContainer();

      // Update size of terminal
      mScreen.doResizeIfNecessary();
      var currSize = toIpoint(mScreen.getTerminalSize());
      if (!currSize.equals(mPrevLayoutScreenSize)) {
        mPrevLayoutScreenSize = currSize;
        c.setTotalBounds(new IRect(currSize));
        c.setLayoutInvalid();
      }

      //      m.chooseFocus();
      updateView(c);

      // Make changes visible
      mScreen.refresh();
    } catch (Throwable t) {
      m.closeIfError(t);
      throw asRuntimeException(t);
    }
  }

  public boolean quitRequested() {
    return mQuitFlag;
  }

  public void quit() {
    mQuitFlag = true;
  }

  private boolean mQuitFlag;

  private AbstractScreen mScreen = JScreen.sharedInstance().screen();

  static {
    SHARED_INSTANCE = new WinMgr();
  }

  private static IPoint toIpoint(TerminalSize s) {
    return IPoint.with(s.getColumns(), s.getRows());
  }

  /**
   * If a view's layout is invalid, calls its layout() method, and invalidates
   * its paint.
   * 
   * If the view's paint is invalid, renders it.
   * 
   * Recursively processes all child views in this manner as well.
   */
  private void updateView(JWindow w) {
    final boolean db = false && alert("logging is on");

    //    winMgr().addFocus(w);

    if (db) {
      if (!w.layoutValid() || !w.paintValid())
        pr(VERT_SP, "updateViews");
    }

    if (!w.layoutValid()) {
      if (db)
        pr("...window", w.name(), "layout is invalid");
      w.repaint();
      w.layout();
      w.setLayoutValid();

      // Invalidate layout of any child views as well
      for (var c : w.children())
        c.setLayoutInvalid();
    }

    if (!w.paintValid()) {
      // We are repainting everything, so make the partial valid as well
      w.setPartialPaintValid(true);
      if (db)
        pr("...window", w.name(), "paint is invalid; rendering; bounds:", w.totalBounds());
      w.render(false);
      w.setPaintValid(true);
    } else if (!w.partialPaintValid()) {
      if (db)
        pr("...window", w.name(), "partial paint is invalid");
      w.render(true);
      w.setPartialPaintValid(true);
    }

    for (var c : w.children())
      updateView(c);
  }

  /**
   * Restore normal terminal window if an exception is not null (so that
   * subsequent dumping of the exception will actually appear to the user in the
   * normal terminal window)
   */
  public Throwable closeIfError(Throwable t) {
    if (t != null)
      JScreen.sharedInstance().close();
    return t;
  }

  private IPoint mPrevLayoutScreenSize;
}
