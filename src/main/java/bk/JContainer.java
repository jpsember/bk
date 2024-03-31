package bk;

import static js.base.Tools.*;

import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.MyMath;

/**
 * A window that contains other windows
 */
public class JContainer extends JWindow {

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
  public void layout() {
    final boolean db = false;

    var problem = false;

    if (children().isEmpty())
      return;

    // Calculate bounds of children

    if (db)
      pr(VERT_SP, "layout:", name(), " for children:", children().size());

    // True if we need to exchange x<->y and w<->h so we can always deal with x being the dynamic dimension
    boolean swap = !mHorzFlag;

    var boundsWithinScreen = calcContentBounds();
    pr("contentBounds:", boundsWithinScreen);

    // The size of the container (normalized so windows are stacked horizontally)

    var normSize = swapIf(boundsWithinScreen.size(), swap);

    // The position of the next child
    var normNextPosition = swapIf(boundsWithinScreen.location(), swap);

    // Determine the space to distribute to the fixed-width windows,
    // as well as the sum of the percentages of the dynamic-width windows

    double pctSum = 0;
    int charsSum = 0;
    for (var child : children()) {
      var sizeExpr = child.getSizeExpr();
      if (sizeExpr > 0)
        charsSum += sizeExpr;
      else
        pctSum += -sizeExpr;
    }
    pctSum = Math.max(1, pctSum);

    var charsDynamicTotal = Math.max(0, normSize.x - charsSum);

    if (db)
      pr("charsSum:", charsSum, "pctSum:", pctSum, "charsDynamic:", charsDynamicTotal);

    var staticCharsAllotted = 0;
    int dynamicCharsAllotted = 0;

    for (JWindow c : children()) {
      if (db)
        pr(VERT_SP, "layout next child");
      var sizeExpr = c.getSizeExpr();

      int chars;
      if (sizeExpr > 0) {
        chars = sizeExpr;
        chars = MyMath.clamp(chars, 0, normSize.x - staticCharsAllotted);
        staticCharsAllotted += chars;
      } else {
        var target = (charsDynamicTotal * -sizeExpr) / pctSum;
        chars = (int) Math.round(target);
        chars = MyMath.clamp(chars, 0, charsDynamicTotal - dynamicCharsAllotted);
        dynamicCharsAllotted += chars;
        if (db)
          pr("...pct:", -sizeExpr, "dynamicTot:", charsDynamicTotal, "target:", target);
      }
      if (db)
        pr("sizeExpr:", sizeExpr, "chars:", chars);
      if (db)
        pr("charsDynamicTotal:", charsDynamicTotal, "clamped chars:", chars);
      if (chars == 0) {
        alert("problem fitting window");
      }

      var ourNormSize = new IPoint(chars, normSize.y);
      var ourNormBounds = IRect.withLocAndSize(normNextPosition, ourNormSize);
      if (db)
        pr("ourNormSize:", ourNormSize, "ourNormBounds:", ourNormBounds);

      var ourBounds = swapIf(ourNormBounds, swap);
      c.setTotalBounds(ourBounds);
      if (db)
        pr("...child bounds:", ourBounds);
      normNextPosition = normNextPosition.sumWith(ourNormSize.x, 0);
    }
    if (problem)
      alert("there was a layout problem");
  }

  boolean mHorzFlag;

}
