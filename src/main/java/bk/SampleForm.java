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
  public void setWindow(JWindow window) {
    mWindow = window;
  }

  @Override
  public void paint() {
    var r = Render.SHARED_INSTANCE;
    pr("TextFieldHandler, paint, partial:", r.partial());
    te().render();
  }

  @Override
  public void loseFocus() {
    screen().hideCursor();
  }

  @Override
  public void gainFocus() {
    var s = screen();
    todo("gainFocus");
    mWindow.repaintPartial();
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
  private JWindow mWindow;
}