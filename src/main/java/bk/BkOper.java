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

    //    mScreen.window().setHandler(new WindowHandler() {
    //      @Override
    //      public void paint(JWindow window) {
    //        pr("painting, window:", window);
    //        window.drawRect(new IRect(window.bounds().size()));
    //        pr("done drawrect");
    //      }
    //    });
    //    

    var screen = screen();
    try {
      loadUtil();
      todo("when do we set the screen size?");
      pr("calling screen.open()....");
      screen.open();
      pr("...finished screen.open()");

      var mgr = winMgr();

      // Create a root container
      mgr.pushContainer();
      {
        // Construct two windows; the second has some horizontal panels
        pr("constructing out first window with size=75");
        mgr.pct(75);
        mgr.window();
        pr("constructing second container with size=25");
        mgr.pct(25);
        if (true) {
          mgr.horz().pushContainer();
          {
            mgr.chars(15).window();
            mgr.pct(80).window();
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

  //  @Override
  //  public void repaint() {
  //    todo("repaint");
  //    if (!mScreenValid) {
  //      mScreenValid = true;
  //      //      IRect bounds = new IRect(mScreen.screenSize());
  //      //      var w = mScreen.window();
  //      //      w.setBounds(bounds);
  //      //      w.repaint();
  //    }
  //  }

  //  @Override
  //  public void processKey(KeyStroke keyStroke) {
  //    todo("processKey:", keyStroke);
  //    if (keyStroke.getKeyType() == KeyType.Escape)
  //      mScreen.quit();
  //  }
  //
  //  @Override
  //  public void processNewSize(IPoint size) {
  //    todo("processNewSize:", size);
  //    mScreenValid = false;
  //  }

  private BkConfig mConfig;
  //  private JScreen mScreen;
  //  private boolean mScreenValid;

}
