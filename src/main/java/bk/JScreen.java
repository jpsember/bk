package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.Random;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextCharacter;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.screen.AbstractScreen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import js.file.Files;
import js.geometry.IPoint;

/**
 * Wrapper for lanterna terminal / screen
 */
public class JScreen {

  private static JScreen SHARED_INSTANCE;

  public static JScreen sharedInstance() {
    if (SHARED_INSTANCE == null)
      badState("no shared instance");
    return SHARED_INSTANCE;
  }

  public JScreen(ScreenHandler handler) {
    todo("This should be a singleton");
    checkState(SHARED_INSTANCE == null, "already built");
    mHandler = handler;
    SHARED_INSTANCE = this;
  }

  //  public JWindow window() {
  //    if (mMainWindow == null) {
  //      var w = new JWindow();
  //      mMainWindow = w;
  //    }
  //    return mMainWindow;
  //  }

  public void open() {
    try {
      DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
      mTerminal = defaultTerminalFactory.createTerminal();
      mScreen = new TerminalScreen(mTerminal);
      mScreen.startScreen();
      // Turn off cursor for now
      mScreen.setCursorPosition(null);
      pr("set screen to:", mScreen);

      // Open a window manager for the screen

      mWindowManager = new WinMgr();
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  public void close() {
    if (mScreen == null)
      return;
    msg("closing screen");
    Files.close(mScreen);
    // There seems to be a problem with restoring the cursor position; it positions the cursor at the end of the last line
    pr();
    mScreen = null;
    mTerminal = null;
  }

  public boolean isOpen() {
    return mScreen != null;
  }

  public void update() {
    try {
      KeyStroke keyStroke = mScreen.pollInput();
      if (keyStroke != null) {
        mHandler.processKey(keyStroke);
      }

      // Update size of terminal
      mScreen.doResizeIfNecessary();
      var currSize = toIpoint(mScreen.getTerminalSize());
      if (!currSize.equals(mScreenSize)) {
        mScreenSize = currSize;
        mHandler.processNewSize(mScreenSize);
      }

      if (!quitRequested()) {
        todo("no longer calling window handler's repaint method");
        //        mHandler.repaint();

        //   performPaint(window(), true);

        // Make changes visible
        mScreen.refresh();
      }
    } catch (Throwable t) {
      closeIfError(t);
      throw asRuntimeException(t);
    }
  }

  public AbstractScreen screen() {
    return mScreen;
  }

  public WinMgr windowManager() {
    return mWindowManager;
  }

  public IPoint screenSize() {
    if (mScreenSize == null) {
      mScreenSize = toIpoint(mScreen.getTerminalSize());
      pr("screen size is:", mScreenSize);
    }
    return mScreenSize;
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
    msg("mainLoop start");
    while (isOpen()) {
      update();
      sleepMs(10);
      if (quitRequested())
        close();
    }
    msg("done main loop");
  }

  public void drawRandomContent() {
    random = new Random();
    TerminalSize terminalSize = mScreen.getTerminalSize();
    for (int column = 0; column < terminalSize.getColumns(); column++) {
      for (int row = 0; row < terminalSize.getRows(); row++) {
        mScreen.setCharacter(column, row, TextCharacter.fromCharacter(' ', TextColor.ANSI.DEFAULT,
            // This will pick a random background color
            TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)])[0]);
      }
    }
  }

  public void updateRandomContent() {
    var termSize = mScreen.getTerminalSize();
    // Increase this to increase speed
    final int charactersToModifyPerLoop = 1;
    for (int i = 0; i < charactersToModifyPerLoop; i++) {
      /*
       * We pick a random location
       */
      TerminalPosition cellToModify = new TerminalPosition(random.nextInt(termSize.getColumns()),
          random.nextInt(termSize.getRows()));

      /*
       * Pick a random background color again
       */
      TextColor.ANSI color = TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)];

      /*
       * Update it in the back buffer, notice that just like TerminalPosition
       * and TerminalSize, TextCharacter objects are immutable so the
       * withBackgroundColor(..) call below returns a copy with the background
       * color modified.
       */
      TextCharacter characterInBackBuffer = mScreen.getBackCharacter(cellToModify);
      characterInBackBuffer = characterInBackBuffer.withBackgroundColor(color);
      characterInBackBuffer = characterInBackBuffer.withCharacter(' '); // Because of the label box further down, if it shrinks
      mScreen.setCharacter(cellToModify, characterInBackBuffer);
    }

    /*
     * Just like with Terminal, it's probably easier to draw using TextGraphics.
     * Let's do that to put a little box with information on the size of the
     * terminal window
     */
    String sizeLabel = "Terminal Size: " + termSize;
    TerminalPosition labelBoxTopLeft = new TerminalPosition(1, 1);
    TerminalSize labelBoxSize = new TerminalSize(sizeLabel.length() + 2, 3);
    TerminalPosition labelBoxTopRightCorner = labelBoxTopLeft
        .withRelativeColumn(labelBoxSize.getColumns() - 1);
    TextGraphics textGraphics = mScreen.newTextGraphics();
    //This isn't really needed as we are overwriting everything below anyway, but just for demonstrative purpose
    textGraphics.fillRectangle(labelBoxTopLeft, labelBoxSize, ' ');

    /*
     * Draw horizontal lines, first upper then lower
     */
    textGraphics.drawLine(labelBoxTopLeft.withRelativeColumn(1),
        labelBoxTopLeft.withRelativeColumn(labelBoxSize.getColumns() - 2), Symbols.DOUBLE_LINE_HORIZONTAL);
    textGraphics.drawLine(labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(1),
        labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(labelBoxSize.getColumns() - 2),
        Symbols.DOUBLE_LINE_HORIZONTAL);

    /*
     * Manually do the edges and (since it's only one) the vertical lines, first
     * on the left then on the right
     */
    textGraphics.setCharacter(labelBoxTopLeft, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
    textGraphics.setCharacter(labelBoxTopLeft.withRelativeRow(1), Symbols.DOUBLE_LINE_VERTICAL);
    textGraphics.setCharacter(labelBoxTopLeft.withRelativeRow(2), Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
    textGraphics.setCharacter(labelBoxTopRightCorner, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
    textGraphics.setCharacter(labelBoxTopRightCorner.withRelativeRow(1), Symbols.DOUBLE_LINE_VERTICAL);
    textGraphics.setCharacter(labelBoxTopRightCorner.withRelativeRow(2),
        Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER);

    /*
     * Finally put the text inside the box
     */
    textGraphics.putString(labelBoxTopLeft.withRelative(1, 1), sizeLabel);
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

  private void performPaint(JWindow w, boolean validFlag) {
    if (validFlag && w.paintValid())
      return;
    w.setPaintValid(true);
    var h = w.handler();
    h.paint(w);
    for (var c : w.children()) {
      performPaint(c, false);
    }

  }

  private Random random;
  private Terminal mTerminal;
  private AbstractScreen mScreen;
  private ScreenHandler mHandler;
  private IPoint mScreenSize;
  private boolean mQuitFlag;
  private WinMgr mWindowManager;
}
