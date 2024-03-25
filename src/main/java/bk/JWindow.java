package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import com.googlecode.lanterna.Symbols;

import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.MyMath;

public class JWindow {

  private WindowHandler mHandler;

  public void setHandler(WindowHandler h) {
    mHandler = h;
  }

  public WindowHandler handler() {
    return nullTo(mHandler, DEFAULT_HANDLER);
  }

  public IRect bounds() {
    return mBounds;
  }

  public List<JWindow> children() {
    return mChildren;
  }

  public void setBounds(IRect bounds) {
    mBounds = bounds;
    todo("Resize any child windows as well");
  }

  public boolean paintValid() {
    return mPaintValid;
  }

  public void setPaintValid(boolean f) {
    mPaintValid = f;
  }

  public void repaint() {
    setPaintValid(false);
  }

  public void drawRect(IRect bounds) {
    var js = screen();
    var s = js.screen();
    var cb = clipToWindowBounds(bounds);
    if (cb.width == 0 || cb.height == 0)
      return;

    var tg = s.newTextGraphics();
    var min = toTerm(cb.x, cb.y);
    var max = toTerm(cb.endX(), cb.endY());
    if (cb.width > 2) {
      tg.drawLine(min.x + 1, min.y, max.x - 2, min.y, Symbols.DOUBLE_LINE_HORIZONTAL);
      tg.drawLine(min.x + 1, max.y - 1, max.x - 2, max.y - 1, Symbols.DOUBLE_LINE_HORIZONTAL);
    }
    if (cb.height > 2) {
      tg.drawLine(min.x, min.y + 1, min.x, max.y - 2, Symbols.DOUBLE_LINE_VERTICAL);
      tg.drawLine(max.x - 1, min.y + 1, max.x - 1, max.y - 2, Symbols.DOUBLE_LINE_VERTICAL);
    }
    tg.setCharacter(min.x, min.y, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
    tg.setCharacter(min.x, max.y - 1, Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
    tg.setCharacter(max.x - 1, min.y, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
    tg.setCharacter(max.x - 1, max.y - 1, Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER);
  }

  public IPoint toTerm(int wx, int wy) {
    return new IPoint(wx + mBounds.x, wy + mBounds.y);
  }

  public IPoint clampToWindowBounds(IPoint pt) {
    var x = MyMath.clamp(pt.x, 0, mBounds.width);
    var y = MyMath.clamp(pt.y, 0, mBounds.height);
    if (x != pt.x || y != pt.y)
      pt = new IPoint(x, y);
    return pt;
  }

  private IRect clipToWindowBounds(IRect bounds) {
    var min = clampToWindowBounds(bounds.bottomLeft());
    var max = clampToWindowBounds(bounds.topRight());
    return new IRect(min, max);
  }

  private IRect mBounds;
  private List<JWindow> mChildren = arrayList();
  private boolean mPaintValid;

  private static final WindowHandler DEFAULT_HANDLER = new WindowHandler() {

    @Override
    public void paint(JWindow window) {

    }
  };
}
