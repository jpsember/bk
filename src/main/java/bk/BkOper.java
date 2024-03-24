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
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import bk.gen.BkConfig;
import js.app.AppOper;
import js.base.BasePrinter;
import js.file.Files;

public class BkOper extends AppOper {

  @Override
  public String userCommand() {
    return "bk";
  }

  @Override
  protected String shortHelp() {
    return "Bookkeeping program";
  }

  @Override
  public BkConfig defaultArgs() {
    return BkConfig.DEFAULT_INSTANCE;
  }

  @Override
  protected void longHelp(BasePrinter b) {
    todo("more longHelp to come later...");
    // TODO Auto-generated method stub
    super.longHelp(b);
  }

  @Override
  public BkConfig config() {
    if (mConfig == null)
      mConfig = (BkConfig) super.config();
    return mConfig;
  }

  private BkConfig mConfig;

  @Override
  public void perform() {
    loadUtil();
    DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
    Terminal terminal = null;
    Screen screen = null;
    try {
      terminal = defaultTerminalFactory.createTerminal();
      screen = new TerminalScreen(terminal);
      screen.startScreen();

      // Turn off cursor for now
      screen.setCursorPosition(null);

      // Draw some random content
      Random random = new Random();
      TerminalSize terminalSize = screen.getTerminalSize();
      for (int column = 0; column < terminalSize.getColumns(); column++) {
        for (int row = 0; row < terminalSize.getRows(); row++) {
          screen.setCharacter(column, row, TextCharacter.fromCharacter(' ', TextColor.ANSI.DEFAULT,
              // This will pick a random background color
              TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)])[0]);
        }
      }

      // Make changes visible
      screen.refresh();

      /*
       * Ok, now we loop and keep modifying the screen until the user exits by
       * pressing escape on the keyboard or the input stream is closed. When
       * using the Swing/AWT bundled emulator, if the user closes the window
       * this will result in an EOF KeyStroke.
       */
      while (true) {
        KeyStroke keyStroke = screen.pollInput();
        if (keyStroke != null
            && (keyStroke.getKeyType() == KeyType.Escape || keyStroke.getKeyType() == KeyType.EOF)) {
          break;
        }

        // Update size of terminal
        TerminalSize newSize = screen.doResizeIfNecessary();
        if (newSize != null) {
          terminalSize = newSize;
        }

        // Increase this to increase speed
        final int charactersToModifyPerLoop = 1;
        for (int i = 0; i < charactersToModifyPerLoop; i++) {
          /*
           * We pick a random location
           */
          TerminalPosition cellToModify = new TerminalPosition(random.nextInt(terminalSize.getColumns()),
              random.nextInt(terminalSize.getRows()));

          /*
           * Pick a random background color again
           */
          TextColor.ANSI color = TextColor.ANSI.values()[random.nextInt(TextColor.ANSI.values().length)];

          /*
           * Update it in the back buffer, notice that just like
           * TerminalPosition and TerminalSize, TextCharacter objects are
           * immutable so the withBackgroundColor(..) call below returns a copy
           * with the background color modified.
           */
          TextCharacter characterInBackBuffer = screen.getBackCharacter(cellToModify);
          characterInBackBuffer = characterInBackBuffer.withBackgroundColor(color);
          characterInBackBuffer = characterInBackBuffer.withCharacter(' '); // Because of the label box further down, if it shrinks
          screen.setCharacter(cellToModify, characterInBackBuffer);
        }

        /*
         * Just like with Terminal, it's probably easier to draw using
         * TextGraphics. Let's do that to put a little box with information on
         * the size of the terminal window
         */
        String sizeLabel = "Terminal Size: " + terminalSize;
        TerminalPosition labelBoxTopLeft = new TerminalPosition(1, 1);
        TerminalSize labelBoxSize = new TerminalSize(sizeLabel.length() + 2, 3);
        TerminalPosition labelBoxTopRightCorner = labelBoxTopLeft
            .withRelativeColumn(labelBoxSize.getColumns() - 1);
        TextGraphics textGraphics = screen.newTextGraphics();
        //This isn't really needed as we are overwriting everything below anyway, but just for demonstrative purpose
        textGraphics.fillRectangle(labelBoxTopLeft, labelBoxSize, ' ');

        /*
         * Draw horizontal lines, first upper then lower
         */
        textGraphics.drawLine(labelBoxTopLeft.withRelativeColumn(1),
            labelBoxTopLeft.withRelativeColumn(labelBoxSize.getColumns() - 2),
            Symbols.DOUBLE_LINE_HORIZONTAL);
        textGraphics.drawLine(labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(1),
            labelBoxTopLeft.withRelativeRow(2).withRelativeColumn(labelBoxSize.getColumns() - 2),
            Symbols.DOUBLE_LINE_HORIZONTAL);

        /*
         * Manually do the edges and (since it's only one) the vertical lines,
         * first on the left then on the right
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

        /*
         * Ok, we are done and can display the change. Let's also be nice and
         * allow the OS to schedule other threads so we don't clog up the core
         * completely.
         */
        screen.refresh();
        sleepMs(50);
      }
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
    Files.close(screen);
    // There seems to be a problem with restoring the cursor position; it positions the cursor at the end of the last line
    pr();
  }

}
