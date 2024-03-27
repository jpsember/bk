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

  @Override
  public void render() {
    // The child views will render themselves.

    // Special case: if this is the top level container, clear its bounds.
    if (this == WinMgr.SHARED_INSTANCE.rootContainer())
      clearRect(bounds().withLocation(IPoint.ZERO));
  }

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

  @Override
  public void layout(IRect boundsWithinScreen) {
    final boolean db = true;

    var problem = false;

    super.layout(boundsWithinScreen);

    // Layout any children
    if (!children().isEmpty()) {

      if (db)
        pr(VERT_SP, "layout for children:", children().size());

      // True if we need to exchange x<->y and w<->h so we can always deal with x being the dynamic dimension
      boolean swap = !mHorzFlag;

      // The size of the container (normalized so windows are stacked horizontally)

      var normSize = swapIf(boundsWithinScreen.size(), swap);

      // The position of the next child
      var normNextPosition = swapIf(boundsWithinScreen.location(), swap);

      // Determine the space to distribute to the fixed-width windows,
      // as well as the sum of the percentages of the dynamic-width windows

      int pctSum = 0;
      int charsSum = 0;
      for (var child : children()) {
        child.setLayoutInvalid();
        var sizeExpr = child.getSizeExpr();
        if (sizeExpr > 0)
          charsSum += sizeExpr;
        else
          pctSum += -sizeExpr;
      }
      pctSum = Math.max(1, pctSum);

      var charsDynamicTotal = Math.max(0, normSize.x - charsSum);
      //      var excess = Math.max(0, normSize.x - charsSum);

      if (db)
        pr("charsSum:", charsSum, "pctSum:", pctSum, "charsDynamic:", charsDynamicTotal);

      var staticCharsAllotted = 0;

      for (JWindow c : children()) {
        if (db)
          pr(VERT_SP, "layout out next child");
        var sizeExpr = c.getSizeExpr();

        int chars;
        if (sizeExpr > 0)
          chars = sizeExpr;
        else
          chars = (charsDynamicTotal * -sizeExpr) / pctSum;
        if (db)
          pr("sizeExpr:", sizeExpr, "chars:", chars);
        chars = MyMath.clamp(chars, 0, normSize.x - staticCharsAllotted);
        if (db)
          pr("charsDynamicTotal:", charsDynamicTotal, "clamped chars:", chars);
        if (chars == 0) {
          alert("problem fitting window");
        }

        if (sizeExpr > 0)
          staticCharsAllotted += chars;

        var ourNormSize = new IPoint(chars, normSize.y);
        var ourNormBounds = IRect.withLocAndSize(normNextPosition, ourNormSize);
        if (db)
          pr("ourNormSize:", ourNormSize, "ourNormBounds:", ourNormBounds);

        var ourBounds = swapIf(ourNormBounds, swap);
        c.setBounds(ourBounds);
        if (db)
          pr("...child bounds:", ourBounds);
        normNextPosition = normNextPosition.sumWith(ourNormSize.x, 0);

        // Layout the children as well
        c.layout(ourBounds);
      }
      if (problem)
        alert("there was a layout problem");
    }
  }

  boolean mHorzFlag;

}
