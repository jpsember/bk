package bk;

import js.geometry.IPoint;
import js.geometry.IRect;

import static js.base.Tools.*;

/**
 * A window that contains other windows
 *
 */
public class JContainer extends JWindow {

  public void layout(IPoint screenLocation, IPoint size) {
    todo("layout child windows");
    //    var parentPosition = IPoint.ZERO;
    var b = IRect.withLocAndSize(screenLocation, size);
    setBounds(b);
    if (children().size() != 1)
      badArg("expected single child for now");
    var w = children().get(0);
    w.setBounds(b);
  }
}
