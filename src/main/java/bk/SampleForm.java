package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import com.googlecode.lanterna.input.KeyStroke;

/**
 * An experiment.
 * 
 * A JWindow that contains a TextField
 *
 */
public class SampleForm extends JWindow implements FocusHandler {

  public SampleForm() {
    updateVerbose();
    todo("maybe TextEdit should be a JWindow subclass?");
  }

  @Override
  public void paint() {
    var r = Render.SHARED_INSTANCE;
    //    mWind = r.window();
    log("TextFieldHandler, paint, partial:", r.partial());
    te().render();
  }

  @Override
  public void loseFocus() {
    log("loseFocus");
    screen().hideCursor();
  }

  @Override
  public void gainFocus() {
    log("gainFocus");
    //    if (mWind == null) {
    //      alert("gainFocus: window hasn't been rendered yet");
    //      return;
    //    }
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

  @Override
  public void processKeyStroke(KeyStroke k) {
    log("processKeyStroke:", k);
  }

  public TextEdit mt;
  //  private JWindow mWind;
}