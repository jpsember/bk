package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import bk.gen.BkConfig;
import js.app.AppOper;
import js.base.BasePrinter;
import js.geometry.IPoint;

public class BkOper extends AppOper implements ScreenHandler {

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

  @Override
  public void perform() {
    mScreen = new JScreen(this);
    try {
      loadUtil();
      mScreen.open();
      mScreen.mainLoop();
    } catch (Throwable t) {
      setError(mScreen.closeIfError(t));
    }
  }

  @Override
  public void repaint() {
    todo("repaint");
    if (!mDrawn) {
      mScreen.drawRandomContent();
      mDrawn = true;
    }
    mScreen.updateRandomContent();
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
  }

  private BkConfig mConfig;
  private boolean mDrawn;
  private JScreen mScreen;

}
