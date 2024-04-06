package bk;

import bk.gen.HelperResult;
import js.base.BaseObject;
import js.geometry.MyMath;

public class WidgetHelper extends BaseObject {

  public WidgetHelper() {
    alertVerbose();
  }

  void setWidget(WidgetWindow widget) {
    mWidget = widget;
  }

  //  public void paint() {
  //    var b = determineBounds();
  //    if (b == null)
  //      return;
  //
  //    var r = Render.SHARED_INSTANCE;
  //    var bSave = r.clipBounds();
  //    {
  //      // Save the current clip rect since we may be rendering outside of it
  //
  //      r.setClipBounds(b);
  //      r.drawRect(b, BORDER_ROUNDED);
  //    }
  //    r.setClipBounds(bSave);
  //
  //  }

  //  private IRect determineBounds() {
  //
  //    var w = mWidget;
  //    var content = w.getContentForHelper();
  //    if (content.isEmpty()) {
  //      return null;
  //    }
  //    var r = Render.SHARED_INSTANCE;
  //    var wb = r.clipBounds();
  //    IPoint size = new IPoint(30, 6);
  //    var locX = wb.x + 30;
  //    var locY = wb.y - (size.y + 2);
  //    if (locY < 0)
  //      locY = wb.y + 2;
  //    return new IRect(locX, locY, size.x, size.y);
  //  }

  public HelperResult processKeyEvent(String hint, KeyEvent k) {

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

  private WidgetWindow mWidget;
  private int mPosition;
  private int mSize = 5;
}
