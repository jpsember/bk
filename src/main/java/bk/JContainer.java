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

    var problem = false;

    var b = IRect.withLocAndSize(screenLocation, size);
    setBounds(b);

    // Layout any children
    if (!children().isEmpty()) {

      // int otherDim = mHorzFlag ? size.y : size.x;
      int varDim = mHorzFlag ? size.x : size.y;

      int pctSum = 0;
      int charsSum = 0;
      for (var c : children()) {
        var s = c.mSizer;
        var chars = s.getChars(mHorzFlag);
        if (chars >= 0)
          charsSum += chars;
        else
          pctSum += s.getPct(mHorzFlag);
      }
      pctSum = Math.max(1, pctSum);

      var charsRem = varDim;
      var excess = Math.max(0, charsRem - charsSum);

      int xoff = b.x;
      int yoff = b.y;

      for (var c : children()) {
        var s = c.mSizer;
        var chars = s.getChars(mHorzFlag);
        if (chars >= 0) {
          if (chars > charsRem) {
            problem = true;
            alert("trouble fitting window, wanted:", chars, "but remaining:", charsRem);
            chars = charsRem;
          }
        } else {
          if (excess == 0) {
            alert("trouble fitting window, no stretch space remaining");
            problem = true;
            chars = charsRem;
          } else {
            chars = (excess * s.getPct(mHorzFlag)) / pctSum;
          }
        }
        if (chars <= 0) {
          alert("problem fitting window");
          chars = 0;
        }
        int charsWidth = size.x;
        int charsHeight = size.y;
        if (mHorzFlag)
          charsWidth = chars;
        else
          charsHeight = chars;

        c.setBounds(new IRect(xoff, yoff, charsWidth, charsHeight));
        if (mHorzFlag)
          xoff += charsWidth;
        else
          yoff += charsHeight;
      }
      if (problem)
        alert("there was a layout problem");
    }
  }

  boolean mHorzFlag;

}
