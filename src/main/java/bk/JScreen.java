package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import com.googlecode.lanterna.screen.AbstractScreen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import js.file.Files;

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
      winMgr().hideCursor();
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

  public AbstractScreen screen() {
    return mScreen;
  }

  private Terminal mTerminal;
  private AbstractScreen mScreen;

}
