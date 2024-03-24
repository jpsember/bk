package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
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
    DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
    Terminal terminal = null;
    try {
      terminal = defaultTerminalFactory.createTerminal();
      terminal.enterPrivateMode();
      //      terminal.clearScreen();
      terminal.putString("Hello\n");
      terminal.flush();

      terminal.enableSGR(SGR.REVERSE);
      terminal.putString("SGR.REVERSE\n");
      terminal.resetColorAndSGR();

      terminal.enableSGR(SGR.BOLD);
      terminal.putString("SGR.BOLD\n");
      terminal.resetColorAndSGR();

      terminal.enableSGR(SGR.BORDERED);
      terminal.putString("SGR.BORDERED\n");
      terminal.resetColorAndSGR();

      terminal.flush();

      terminal.resetColorAndSGR();
      terminal.setCursorPosition(terminal.getCursorPosition().withColumn(0).withRelativeRow(1));
      terminal.putCharacter('D');
      terminal.putCharacter('o');
      terminal.putCharacter('n');
      terminal.putCharacter('e');
      terminal.putCharacter('\n');
      terminal.flush();

      while (true) {
        KeyStroke keyStroke = terminal.pollInput();
        if (keyStroke == null)
          continue;
        if (keyStroke.getKeyType() == KeyType.Escape)
          break;

        pr("keyStroke:", keyStroke);
        var ch = keyStroke.getCharacter();
        if (ch != null) {
          pr("ch:", ch);
        }
      }
      terminal.exitPrivateMode();
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
    Files.close(terminal);
    if (false)
      sleepMs(1);
  }

}
