package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import bk.gen.BkConfig;
import js.app.AppOper;
import js.base.BasePrinter;

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
    var screen = screen();
    try {
      loadUtil();
      screen.open();

      var mgr = winMgr();

      // Create a root container
      mgr.pushContainer();
      {
        // Construct two windows; the second has some horizontal panels
        mgr.pct(75);
        mgr.thickBorder();
        mgr.window();
        mgr.pct(25);
        {
          mgr.horz().pushContainer();
          {
            mgr.chars(15).window();
            mgr.roundedBorder();
            mgr.pct(80).window();
            mgr.thinBorder();
            mgr.handler(new WindowHandler() {
              @Override
              public void paint(JWindow w) {
                var r = w.bounds().withInset(2);
                pr(w.name(), "painting rect:", r);
                if (r.isValid())
                  w.drawRect(r, BORDER_ROUNDED);
              }
            });
            mgr.pct(20).window();
          }
          mgr.popContainer();
        }
      }
      mgr.doneConstruction();
      screen.mainLoop();
    } catch (Throwable t) {
      setError(screen.closeIfError(t));
    }
  }

  private BkConfig mConfig;

}
