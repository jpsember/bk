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
      //
      //      // Open a window manager for the screen
      //
      //      mWindowManager = new WinMgr();
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  public void close() {
    if (mScreen == null)
      return;
    msg("closing screen");
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
      KeyStroke keyStroke = mScreen.pollInput();
      if (keyStroke != null) {
        pr("got:", keyStroke);
        if (keyStroke.getKeyType() == KeyType.Escape) {
          quit();
          return;
        }
        todo("send key event to the window that has focus");
        //        mHandler.processKey(keyStroke);
      }

      var m = winMgr();
      var c = m.topLevelContainer();

      // Update size of terminal
      mScreen.doResizeIfNecessary();
      var currSize = toIpoint(mScreen.getTerminalSize());
      if (!currSize.equals(mPrevLayoutScreenSize)) {
        mTextGraphics = null;
        mPrevLayoutScreenSize = currSize;

        pr("screen size has changed; invalidating top level window; new size:", currSize);

        c.setBounds(new IRect(currSize));
        c.setLayoutInvalid();

        //        pr("laying out views for screen size:", currSize);
        //        layoutViews(currSize);
      }

      updateView(c);

      // Make changes visible
      mScreen.refresh();
      //      sleepMs(200);
    } catch (Throwable t) {
      closeIfError(t);
      throw asRuntimeException(t);
    }
  }

  public AbstractScreen screen() {
    return mScreen;
  }

  public TextGraphics textGraphics() {
    if (mTextGraphics == null) {
      mTextGraphics = screen().newTextGraphics();
    }
    return mTextGraphics;
  }

  //
  //  public WinMgr windowManager() {
  //    return mWindowManager;
  //  }
  //
  //  public IPoint screenSize() {
  //    if (mScreenSize == null) {
  //      mScreenSize = toIpoint(mScreen.getTerminalSize());
  //      pr("screen size changing to:", mScreenSize);
  //    }
  //    return mScreenSize;
  //  }

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

  /**
   * If a view's layout is invalid, calls its layout() method, and invalidates
   * its paint.
   * 
   * If the view's paint is invalid, renders it.
   * 
   * Recursively processes all child views in this manner as well.
   */
  private void updateView(JWindow w) {
    final boolean db = true && alert("logging is on");

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
      if (db)
        pr("...window", w.name(), "paint is invalid; rendering; bounds:", w.bounds());
      w.render();
      w.setPaintValid(true);
    }
    for (var c : w.children())
      updateView(c);
  }

  private Random random;
  private Terminal mTerminal;
  private AbstractScreen mScreen;
  private IPoint mPrevLayoutScreenSize;
  private boolean mQuitFlag;
  private TextGraphics mTextGraphics;
}
