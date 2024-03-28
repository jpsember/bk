package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;

import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.MyMath;

public final class Render {

  public static final Render SHARED_INSTANCE = new Render();

  private Render() {
  }

  /**
   * Clamp a point to be within the bounds of the window
   */
  private IPoint clampToWindow(int wx, int wy) {
    var cx1 = MyMath.clamp(wx, mClipBounds.x, mClipBounds.endX());
    var cy1 = MyMath.clamp(wy, mClipBounds.y, mClipBounds.endY());
    return new IPoint(cx1, cy1);
  }

  private IRect clampToWindow(IRect r) {
    var p1 = clampToWindow(r.x, r.y);
    var p2 = clampToWindow(r.endX(), r.endY());
    return IRect.rectContainingPoints(p1, p2);
  }

  public IRect clipBounds() {
    return mClipBounds;
  }

  public void setClipBounds(IRect r) {
    mClipBounds = r;
  }

  public JWindow window() {
    return mWindow;
  }

  void prepare(JWindow window) {
    mWindow = window;
    mLayoutBounds = window.layoutBounds();
    mClipBounds = mLayoutBounds;
  }

  public void clearRect(IRect bounds) {
    var p = clampToWindow(bounds);
    if (p.isDegenerate())
      return;
    var tg = textGraphics();
    tg.fillRectangle(new TerminalPosition(p.x, p.y), new TerminalSize(p.width, p.height), ' ');
  }

  private static final char[] sBorderChars = { //
      Symbols.SINGLE_LINE_HORIZONTAL, Symbols.SINGLE_LINE_VERTICAL, Symbols.SINGLE_LINE_TOP_LEFT_CORNER,
      Symbols.SINGLE_LINE_BOTTOM_LEFT_CORNER, Symbols.SINGLE_LINE_TOP_RIGHT_CORNER,
      Symbols.SINGLE_LINE_BOTTOM_RIGHT_CORNER, //

      Symbols.DOUBLE_LINE_HORIZONTAL, Symbols.DOUBLE_LINE_VERTICAL, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER,
      Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER,
      Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER, //

      '╴', Symbols.SINGLE_LINE_VERTICAL, '╭', '╰', '╮', '╯', //
  };

  public void drawString(int x, int y, int maxLength, String s) {
    var b = clipBounds();

    // Determine which substring is within the window bounds
    if (y < b.y || y >= b.endY())
      return;
    var x1 = x;
    var x2 = x + s.length();
    x1 = MyMath.clamp(x1, b.x, b.endX());
    x2 = MyMath.clamp(x2, b.x, b.endX());
    if (x1 >= x2)
      return;

    int sStart = x1 - x;
    int sEnd = Math.min(maxLength, x2 - x);
    if (sEnd <= sStart) {
      return;
    }
    var tg = textGraphics();
    tg.putString(x1, y, s.substring(sStart, sEnd));
  }

  public void drawRect(IRect bounds, int type) {
    checkArgument(type >= 1 && type < BORDER_TOTAL, "unsupported border type:", type);
    int ci = (type - 1) * 6;
    var p = clampToWindow(bounds);
    if (p.width < 2 || p.height < 2)
      return;
    var tg = textGraphics();
    var x1 = p.x;
    var y1 = p.y;
    var x2 = p.endX();
    var y2 = p.endY();
    if (p.width > 2) {
      tg.drawLine(x1 + 1, y1, x2 - 2, y1, sBorderChars[ci + 0]);
      tg.drawLine(x1 + 1, y2 - 1, x2 - 2, y2 - 1, sBorderChars[ci + 0]);
    }
    if (p.height >= 2) {
      tg.drawLine(x1, y1 + 1, x1, y2 - 2, sBorderChars[ci + 1]);
      tg.drawLine(x2 - 1, y1 + 1, x2 - 1, y2 - 2, sBorderChars[ci + 1]);
    }
    tg.setCharacter(x1, y1, sBorderChars[ci + 2]);
    tg.setCharacter(x1, y2 - 1, sBorderChars[ci + 3]);
    tg.setCharacter(x2 - 1, y1, sBorderChars[ci + 4]);
    tg.setCharacter(x2 - 1, y2 - 1, sBorderChars[ci + 5]);
  }

  private IRect mLayoutBounds;
  private IRect mClipBounds;
  private JWindow mWindow;

}
