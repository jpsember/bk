package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import bk.gen.BkConfig;
import js.app.AppOper;
import js.base.BasePrinter;
import js.geometry.IPoint;
import js.geometry.IRect;

public class BkOper extends AppOper implements ScreenHandler {

  @Override
  public String userCommand() {
    todo("have option to output pr to a log file which is tracked by something");
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
    super.longHelp(b);
  }

  @Override
  public BkConfig config() {
    if (mConfig == null)
      mConfig = (BkConfig) super.config();
    return mConfig;
  }

  @Override
  public void perform() {
    mScreen = new JScreen(this);
    mScreen.window().setHandler(new WindowHandler() {
      @Override
      public void paint(JWindow window) {
        pr("painting, window:", window);
        window.drawRect(new IRect(window.bounds().size()));
        pr("done drawrect");
      }
    });
    try {
      loadUtil();
      todo("when do we set the screen size?");
      mScreen.open();
      mScreen.mainLoop();
    } catch (Throwable t) {
      setError(mScreen.closeIfError(t));
    }
  }

  @Override
  public void repaint() {
    todo("repaint");
    if (!mScreenValid) {
      mScreenValid = true;
      IRect bounds = new IRect(mScreen.screenSize());
      var w = mScreen.window();
      w.setBounds(bounds);
      w.repaint();
    }
  }

  @Override
  public void processKey(KeyStroke keyStroke) {
    todo("processKey:", keyStroke);
    if (keyStroke.getKeyType() == KeyType.Escape)
      mScreen.quit();
  }

  @Override
  public void processNewSize(IPoint size) {
    todo("processNewSize:", size);
    mScreenValid = false;
  }

  private BkConfig mConfig;
  private JScreen mScreen;
  private boolean mScreenValid;

}
