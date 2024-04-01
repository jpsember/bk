package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import com.googlecode.lanterna.input.KeyStroke;

import js.geometry.MyMath;

public class MessageWindow extends JWindow {

  public MessageWindow message(String text) {
    mMessage = text;
    return this;
  }

  @Override
  public void paint() {
    if (nullOrEmpty(mMessage))
      return;
    var r = Render.SHARED_INSTANCE;
    var b = r.clipBounds();
    b = b.withInset(1, 0);
    r.drawString(b.x, b.y, b.width, mMessage);
  }

  private String mMessage;

}
