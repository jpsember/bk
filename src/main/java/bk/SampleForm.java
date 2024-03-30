package bk;

import static bk.Util.*;
import static js.base.Tools.*;

/**
 * An experiment.
 * 
 * A JWindow that contains a TextField
 *
 */
public class SampleForm implements WindowHandler, FocusHandler {

  @Override
  public void paint() {
    var r = Render.SHARED_INSTANCE;
//    mWind = r.window();
    pr("TextFieldHandler, paint, partial:", r.partial());
    te().render();
  }

  @Override
  public void loseFocus() {
    screen().hideCursor();
  }

  @Override
  public void gainFocus() {
//    if (mWind == null) {
//      alert("gainFocus: window hasn't been rendered yet");
//      return;
//    }
    var s = screen();
    todo("gainFocus");
    todo("repaint the window containing the sample form?");
    //mWind.repaintPartial();
  }

  private TextEdit te() {
    if (mt == null) {
      var r = Render.SHARED_INSTANCE;
      var wb = r.clipBounds();
      mt = new TextEdit().location(wb.x + 8, wb.y + 3).width(8);
      mt.content("Hello").active(true);
      mt.cursor(-1);
      mt.cursor(5);
    }
    return mt;
  }

  public TextEdit mt;
//  private JWindow mWind;
}