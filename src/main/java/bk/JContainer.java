package bk;

import static js.base.Tools.*;

import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.MyMath;

/**
 * A window that contains other windows
 *
 */
public class JContainer extends JWindow {

  //  private static int getComponent(IPoint pt, boolean horzFlag) {
  //    return horzFlag ? pt.x : pt.y;
  //  }

  private static IPoint swapIf(IPoint pt, boolean swap) {
    if (!swap)
      return pt;
    return IPoint.with(pt.y, pt.x);
  }

  private static IRect swapIf(IRect r, boolean swap) {
    if (!swap)
      return r;
    return new IRect(r.y, r.x, r.height, r.width);
  }

  public void layout(IPoint screenLocation, IPoint size0) {
    todo("layout child windows");

    var problem = false;

    var b = IRect.withLocAndSize(screenLocation, size0);
    setBounds(b);

    // Layout any children
    if (!children().isEmpty()) {

      // True if we need to exchange x<->y and w<->h so we can always deal with x being the dynamic dimension
      boolean swap = mHorzFlag;

      var normSize = swapIf(size0, swap);
      var normNextPosition = swapIf(screenLocation, swap);

      int pctSum = 0;
      int charsSum = 0;
      for (var child : children()) {
        var sizeExpr = child.getSizeExpr();
        if (sizeExpr > 0)
          charsSum += sizeExpr;
        else
          pctSum += -sizeExpr;
      }
      pctSum = Math.max(1, pctSum);

      var excess = Math.max(0, normSize.x - charsSum);

      for (JWindow c : children()) {
        var sizeExpr = c.getSizeExpr();
        int chars;
        if (sizeExpr > 0)
          chars = sizeExpr;
        else
          chars = (excess * -sizeExpr) / pctSum;
        chars = MyMath.clamp(chars, 0, excess);
        if (chars == 0) {
          alert("problem fitting window");
          chars = 0;
        }

        var ourSize = new IPoint(chars, normSize.y);
        var ourBounds = new IRect(normNextPosition, ourSize);

        c.setBounds(swapIf(ourBounds, swap));
        normNextPosition = normNextPosition.sumWith(ourSize.x, 0);
      }
      if (problem)
        alert("there was a layout problem");
    }
  }

  boolean mHorzFlag;

}
