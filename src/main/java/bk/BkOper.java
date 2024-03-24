package bk;

import static js.base.Tools.*;

import com.googlecode.lanterna.SGR;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.WindowBasedTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

import bk.gen.BkConfig;
import js.app.AppOper;
import js.base.BasePrinter;
import js.base.DateTimeTools;
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
    if (false) {
      Screen screen = null;
      Terminal term = null;
      try {
        term = new DefaultTerminalFactory().createTerminal();
        screen = new TerminalScreen(term);
        WindowBasedTextGUI gui = new MultiWindowTextGUI(screen);
        screen.startScreen();
      } catch (Throwable t) {
        throw asRuntimeException(t);
      }

      // use GUI here until the GUI wants to exit

      for (int i = 0; i < 20; i++) {
        pr("i:", i);
        DateTimeTools.sleepForRealMs(1000);
      }

      try {
        screen.stopScreen();
      } catch (Throwable t) {
        throw asRuntimeException(t);
      }
    }

    DefaultTerminalFactory defaultTerminalFactory = new DefaultTerminalFactory();
    Terminal terminal = null;
    try {
      terminal = defaultTerminalFactory.createTerminal();
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
      
      terminal.enableSGR(SGR.ITALIC);
      terminal.putString("SGR.ITALIC\n");
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

      Thread.sleep(2000);
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
    Files.close(terminal);
  }

}
