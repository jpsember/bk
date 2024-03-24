package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;

import bk.gen.BkConfig;
import js.app.AppOper;
import js.base.BasePrinter;

public class BkOper extends AppOper implements KeyHandler, PaintHandler {

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
    s = new JScreen(this, this);
    try {
      loadUtil();
      msg("opening");
      s.open();
      msg("mainLoop...");
      s.mainLoop();
      msg("done main loop");
    } catch (Throwable t) {
      s.crash(t);
      setError(t);
    }
  }

  @Override
  public void repaint() {
    // TODO Auto-generated method stub
    if (!mDrawn) {
      s.drawRandomContent();
      mDrawn = true;
    }
    s.updateRandomContent();
  }

  @Override
  public void processKey(KeyStroke keyStroke) {
    // TODO Auto-generated method stub
    if (keyStroke.getKeyType() == KeyType.Escape)
      s.quit();
  }

  private boolean mDrawn;
  private JScreen s;
}
