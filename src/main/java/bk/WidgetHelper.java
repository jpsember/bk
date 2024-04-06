package bk;

import bk.gen.HelperResult;
import js.base.BaseObject;
import js.geometry.MyMath;

public class WidgetHelper extends BaseObject {

  public WidgetHelper() {
    alertVerbose();
  }

  public HelperResult processKeyEvent(KeyEvent k) {

    var r = HelperResult.newBuilder();

    log("processKeyEvent", k);
    switch (k.toString()) {
    case KeyEvent.ARROW_LEFT:
      move(-1);
      r.text("left!");
      break;
    case KeyEvent.ARROW_RIGHT:
      move(1);
      r.text("right!");
      break;
    case KeyEvent.ENTER:
      r.text("enter!");
      r.selected(true);
      break;
    }
    if (r.text().isEmpty())
      return null;
    return r.build();
  }

  private void move(int direction) {
    mPosition = MyMath.myMod(mPosition + direction, mSize);
  }

  private int mPosition;
  private int mSize = 5;
}
