package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.Stack;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.AbstractScreen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import js.base.BaseObject;
import js.file.Files;
import js.geometry.IPoint;
import js.geometry.IRect;

public class WinMgr extends BaseObject {

  public static final WinMgr SHARED_INSTANCE;

  public WinMgr pushContainer(JContainer container) {
    checkNotNull(container, "expected container");

    // If this is not going to be the top-level window, add it as a child to the current parent.

    if (!mStack.isEmpty()) {
      var parent = container();
      parent.addChild(container);
    }

    applyParam(container);
    push(container);
    return this;
  }

  public WinMgr pushContainer() {
    var container = new JContainer();
    container.mHorzFlag = mHorzFlag;
    mHorzFlag = false;
    return pushContainer(container);
  }

  public WinMgr popContainer() {
    pop();
    return this;
  }

  private void push(JContainer container) {
    checkState(mStack.size() < 100, "stack is too large");

    if (mStack.isEmpty()) {
      t().topLevelContainer = container;
    }

    mStack.push(container);

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

  public WinMgr name(String pendingName) {
    mPendingName = pendingName;
    return this;
  }

  private void pop() {
    if (mStack.isEmpty())
      badState("attempt to pop the outermost container");
    mStack.pop();
  }

  private JContainer container() {
    return mStack.peek();
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
    c.addChild(window);
    applyParam(window);
    return this;
  }

  private void applyParam(JWindow w) {
    w.setSize(mSizeExpr);
    w.setBorder(mBorderType);
    if (!nullOrEmpty(mPendingName))
      w.setName(mPendingName);
    resetPendingWindowVars();
  }

  private void resetPendingWindowVars() {
    mHorzFlag = false;
    mSizeExpr = -100;
    mBorderType = BORDER_NONE;
    mPendingName = null;
  }

  public JContainer topLevelContainer() {
    return t().topLevelContainer;
    //    checkState(mStack.size() == 1, "unexpected stack size:", mStack.size());
    //    return peek(S_TYPE_CONTAINER);
  }

  private boolean mHorzFlag;
  private int mBorderType;
  private int mSizeExpr; // 0: unknown > 1: number of chars < 1: -percentage
  private String mPendingName;

  public void doneConstruction() {
    // Ensure that only the root container remains on the stack
    if (mStack.size() != 0)
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
    while (isOpen()) {
      update();
      sleepMs(30);
      updateFooterMessage();
      storage().flush();
      if (quitRequested())
        close();
    }
  }

  public void update() {
    var m = winMgr();
    try {

      focusManager().update();

      KeyStroke keyStroke = mScreen.pollInput();
      if (keyStroke != null) {
        var key = new KeyEvent(keyStroke);

        boolean processed = false;

        switch (key.toString()) {
        case KeyEvent.QUIT:
          quit();
          return;
        case KeyEvent.ESCAPE:
          if (focusManager().popIfPossible()) {
            processed = true;
          }
          break;
        default:
          if (focusManager().processUndoKeys(key))
            processed = true;
          break;
        }

        if (!processed) {
          focusManager().focus().processKeyEvent(key);
        }
      }

      // Call listeners for any changes that have occurred.  This gives them
      // an opportunity to request a repaint of their views
      changeManager().dispatch();

      var c = m.topLevelContainer();

      // Update size of terminal
      mScreen.doResizeIfNecessary();
      var currSize = toIpoint(mScreen.getTerminalSize());
      if (!currSize.equals(mPrevLayoutScreenSize)) {
        mPrevLayoutScreenSize = currSize;
        c.setTotalBounds(new IRect(currSize));
        c.setLayoutInvalid();
      }

      updateView(c);

      // Make changes visible
      mScreen.refresh();
    } catch (Throwable t) {
      m.closeIfError(t);
      throw asRuntimeException(t);
    }
  }

  private IPoint mPrevLayoutScreenSize;

  public boolean quitRequested() {
    return mQuitFlag;
  }

  public void quit() {
    mQuitFlag = true;
  }

  private boolean mQuitFlag;

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
    final boolean db = false; //&& mark("logging is on");

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
      // Mark all children invalid
      for (var c : w.children())
        c.setPaintValid(false);
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
      close();
    return t;
  }

  // ------------------------------------------------------------------
  // Lanterna screen
  // ------------------------------------------------------------------

  public void open() {
    checkState(mTreeStack == null);
    mTreeStack = new Stack<>();

    openTree();

    try {
      var f = new DefaultTerminalFactory();
      // f.setUnixTerminalCtrlCBehaviour(CtrlCBehaviour.TRAP);
      mTerminal = f.createTerminal();
      mScreen = new TerminalScreen(mTerminal);
      mScreen.startScreen();
      winMgr().hideCursor();
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  private WindowTree mTree;

  private void openTree() {
    if (mTree != null) {
      mTree.mStack = mStack;
      mStack = null;
    }
    var t = new WindowTree();
    mStack = t.mStack;
    mTreeStack.push(t);
  }

  private void closeTree() {
    checkState(!mTreeStack.isEmpty(), "tree stack is empty");
    mTreeStack.pop();
  }

  public void close() {
    if (mScreen == null)
      return;
    Files.close(mScreen);
    closeTree();
    // There seems to be a problem with restoring the cursor position; it positions the cursor at the end of the last line.
    // Probably because our logging doesn't print a linefeed until necessary.
    pr();
    System.out.println();
    mScreen = null;
    mTerminal = null;
  }

  public boolean isOpen() {
    return mScreen != null;
  }

  public AbstractScreen screen() {
    return mScreen;
  }

  private Terminal mTerminal;
  private AbstractScreen mScreen;

  public boolean inView(JWindow window) {
    checkNotNull(window);
    var tc = topLevelContainer();
    while (true) {
      if (window == null)
        break;
      if (window == tc)
        return true;
      window = window.parent();
    }
    return false;
  }

  private Stack<WindowTree> mTreeStack;

  private static class WindowTree {
    Stack<JContainer> mStack = new Stack();
    JContainer topLevelContainer;
  }

  private Stack<JContainer> mStack;

  private WinMgr() {
  }

  private WindowTree t() {
    return mTreeStack.lastElement();
  }

  static {
    SHARED_INSTANCE = new WinMgr();
  }
}
