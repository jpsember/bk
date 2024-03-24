package bk;

import static js.base.Tools.*;

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

}
