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

/**
 * Wrapper for lanterna terminal / screen
 */
public class JScreen {

  public JScreen(KeyHandler keyHandler, PaintHandler paintHandler) {
    mKeyHandler = keyHandler;
    mPaintHandler = paintHandler;
  }

  public void open() {
    try {
      DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
      mTerminal = defaultTerminalFactory.createTerminal();
      mScreen = new TerminalScreen(mTerminal);
      mScreen.startScreen();
      // Turn off cursor for now
      mScreen.setCursorPosition(null);
      pr("set screen to:", mScreen);
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  public void close() {
    if (mScreen == null)
      return;
    pr("closing screen");
    sleepMs(1000);
    Files.close(mScreen);
    // There seems to be a problem with restoring the cursor position; it positions the cursor at the end of the last line
    pr();
    mScreen = null;
    mTerminal = null;
  }

  public boolean isOpen() {
    pr("isOpen:", mScreen != null);
    return mScreen != null;
  }

  public void update() {
    pr("update");
    sleepMs(1000);
    try {
      // badArg("upd");
      KeyStroke keyStroke = mScreen.pollInput();
      if (keyStroke != null) {
        pr("process key");
        sleepMs(1000);
        mKeyHandler.processKey(keyStroke);
      }

      // Update size of terminal
      TerminalSize newSize = mScreen.doResizeIfNecessary();
      if (newSize != null) {
        mTerminalSize = newSize;
      }

      pr("quit req:", quitRequested());
      sleepMs(1000);

      if (!quitRequested()) {
        mPaintHandler.repaint();
        // Make changes visible
        mScreen.refresh();
      }
    } catch (Throwable t) {
      crash(t);
    }
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
      msg("update");
      update();
      sleepMs(10);
      if (quitRequested()) {
        close();
      }
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
    if (mTerminalSize == null)
      return;
    // Increase this to increase speed
    final int charactersToModifyPerLoop = 1;
    for (int i = 0; i < charactersToModifyPerLoop; i++) {
      /*
       * We pick a random location
       */
      TerminalPosition cellToModify = new TerminalPosition(random.nextInt(mTerminalSize.getColumns()),
          random.nextInt(mTerminalSize.getRows()));

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
    String sizeLabel = "Terminal Size: " + mTerminalSize;
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

  public void crash(Throwable t) {
    if (mProblem == null) {
      mProblem = t;
      close();
      // pr("Caught exception:", t.getMessage());
      throw asRuntimeException(t);
    }
  }

  private Random random;
  private Terminal mTerminal;
  private AbstractScreen mScreen;
  private KeyHandler mKeyHandler;
  private PaintHandler mPaintHandler;
  private TerminalSize mTerminalSize;
  private boolean mQuitFlag;
  private Throwable mProblem;
}
