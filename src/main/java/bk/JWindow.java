package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;

import js.base.BaseObject;
import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.MyMath;

public class JWindow extends BaseObject {

  public JWindow() {
    mId = sUniqueId++;
  }

  @Override
  protected String supplyName() {
    return "{W: " + mId + "}";
  }

  private WindowHandler mHandler;

  void setHandler(WindowHandler h) {
    mHandler = h;
  }

  void setId(int id) {
    mId = id;
  }

  WindowHandler handler() {
    return nullTo(mHandler, DEFAULT_HANDLER);
  }

  public IRect layoutBounds() {
    return mLayoutBounds;
  }

  public IRect clipBounds() {
    return mClipBounds;
  }

  List<JWindow> children() {
    return mChildren;
  }

  void setLayoutBounds(IRect bounds) {
    mLayoutBounds = bounds;
  }

  boolean paintValid() {
    return hasFlag(FLG_PAINTVALID);
  }

  void setPaintValid(boolean valid) {
    if (!valid) {
      if (!paintValid())
        return;
      clearFlag(FLG_PAINTVALID);
      // Mark all children as invalid recursively
      for (var c : children())
        c.setPaintValid(false);
    }

    setFlag(FLG_PAINTVALID, valid);
  }

  boolean layoutValid() {
    return hasFlag(FLG_LAYOUTVALID);
  }

  void setLayoutInvalid() {
    clearFlag(FLG_LAYOUTVALID);
  }

  void setLayoutValid() {
    setFlag(FLG_LAYOUTVALID);
  }

  private void setFlag(int f) {
    mFlags |= f;
  }

  private void clearFlag(int f) {
    mFlags &= ~f;
  }

  private boolean hasFlag(int f) {
    return (mFlags & f) != 0;
  }

  private void setFlag(int flag, boolean state) {
    if (!state)
      clearFlag(flag);
    else
      mFlags |= flag;
  }

  public void repaint() {
    setPaintValid(false);
  }

  void layout() {
  }

  /**
   * Render the window onto the screen
   */
  public void render() {
    var layoutBounds = layoutBounds();
    var clipBounds = layoutBounds;
    // Set clip to include the border region; later we might inset it to exclude the border
    mClipBounds = layoutBounds;
    clearRect(layoutBounds);
    int btype = mFlags & FLG_BORDER;

    if (btype != BORDER_NONE) {
      if (alert("experimenting with focus")) {
        if (hasFocus()) {
          btype = BORDER_THICK;
        } else
          btype = BORDER_THIN;
      }
      drawRect(layoutBounds, btype);
      // Now set the clip bounds to exclude the border
      // We inset an extra character horizontally
      clipBounds = clipBounds.withInset(2, 1);
    }
    mClipBounds = clipBounds;
    handler().paint(this);
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

  public final boolean hasFocus() {
    return winMgr().focusWindow() == this;
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

  //  IPoint clampToClip(IPoint pt) {
  //    var r = clipBounds();
  //    var x = MyMath.clamp(pt.x, 0, r.width);
  //    var y = MyMath.clamp(pt.y, 0, r.height);
  //    if (x != pt.x || y != pt.y)
  //      pt = new IPoint(x, y);
  //    return pt;
  //  }

  private IRect mLayoutBounds;
  private IRect mClipBounds;

  private List<JWindow> mChildren = arrayList();

  private static final WindowHandler DEFAULT_HANDLER = new WindowHandler() {
    @Override
    public void paint(JWindow window) {

    }
  };

  final void setSize(int sizeExpr) {
    mSizeExpr = sizeExpr;
  }

  int getSizeExpr() {
    checkArgument(mSizeExpr != 0, "size expression must not be zero");
    return mSizeExpr;
  }

  void setBorder(int type) {
    checkArgument(type >= 0 && type < BORDER_TOTAL);
    mFlags = (mFlags & ~FLG_BORDER) | type;
  }

  int mSizeExpr;

  private int mFlags;
  private static final int FLG_BORDER = 0x3;
  private static final int FLG_PAINTVALID = 1 << 2;
  private static final int FLG_LAYOUTVALID = 1 << 3;

  private static int sUniqueId = 100;
  private int mId;

  public int id() {
    return mId;
  }

}
