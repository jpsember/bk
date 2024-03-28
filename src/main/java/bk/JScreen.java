package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.AbstractScreen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import js.file.Files;
import js.geometry.IPoint;
import js.geometry.IRect;

/**
 * Wrapper for lanterna terminal / screen
 */
public class JScreen {

  private static JScreen SHARED_INSTANCE;

  public static JScreen sharedInstance() {
    if (SHARED_INSTANCE == null) {
      SHARED_INSTANCE = new JScreen();
    }
    return SHARED_INSTANCE;
  }

  private JScreen() {
  }

  public void open() {
    try {
      DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
      mTerminal = defaultTerminalFactory.createTerminal();
      mScreen = new TerminalScreen(mTerminal);
      mScreen.startScreen();
      // Turn off cursor for now
      mScreen.setCursorPosition(null);
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  public void close() {
    if (mScreen == null)
      return;
    Files.close(mScreen);
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

  public void update() {
    try {
      var m = winMgr();
      KeyStroke keyStroke = mScreen.pollInput();
      if (keyStroke != null) {
        if (keyStroke.getKeyType() == KeyType.Escape) {
          todo("Have a special key, like ctrl q, to quit");
          quit();
          return;
        }
        var w = m.focusWindow();
        if (w == null) {
          todo("#10no window has focus");
        } else {
          w.handler().processKeyStroke(w, keyStroke);
        }
      }

      var c = m.topLevelContainer();

      // Update size of terminal
      mScreen.doResizeIfNecessary();
      var currSize = toIpoint(mScreen.getTerminalSize());
      if (!currSize.equals(mPrevLayoutScreenSize)) {
        mTextGraphics = null;
        mPrevLayoutScreenSize = currSize;
        c.setLayoutBounds(new IRect(currSize));
        c.setLayoutInvalid();
      }
      updateView(c);

      // Make changes visible
      mScreen.refresh();
    } catch (Throwable t) {
      closeIfError(t);
      throw asRuntimeException(t);
    }
  }

  public AbstractScreen screen() {
    return mScreen;
  }

  @Deprecated
  public TextGraphics textGraphics() {
    if (mTextGraphics == null) {
      todo("add support for inverse, bold text styles");
      mTextGraphics = screen().newTextGraphics();
    }
    return mTextGraphics;
  }

  private static IPoint toIpoint(TerminalSize s) {
    return IPoint.with(s.getColumns(), s.getRows());
  }

  public boolean quitRequested() {
    return mQuitFlag;
  }

  public void quit() {
    mQuitFlag = true;
  }

  public void mainLoop() {
    while (isOpen()) {
      update();
      sleepMs(10);
      if (quitRequested())
        close();
    }
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
        pr("...window", w.name(), "paint is invalid; rendering; bounds:", w.layoutBounds());
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

  private Terminal mTerminal;
  private AbstractScreen mScreen;
  private IPoint mPrevLayoutScreenSize;
  private boolean mQuitFlag;
  private TextGraphics mTextGraphics;
}
