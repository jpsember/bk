package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import js.base.BaseObject;
import js.geometry.IRect;

public class JWindow extends BaseObject {

  /**
   * Subclasses should override this to supply custom painting. Default does
   * nothing
   */
  public void paint() {
  }

  public JWindow() {
  }

  public final boolean hasFocus() {
    return focusManager().focus() == this;
  }

  public IRect totalBounds() {
    return mWindowBounds;
  }

  List<JWindow> children() {
    return mChildren;
  }

  void setTotalBounds(IRect bounds) {
    mWindowBounds = bounds;
  }

  boolean paintValid() {
    return hasFlag(FLG_PAINTVALID);
  }

  boolean partialPaintValid() {
    return hasFlag(FLG_PARTIALPAINTVALID);
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

  void setPartialPaintValid(boolean valid) {
    setFlag(FLG_PARTIALPAINTVALID, valid);
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

  public void repaintPartial() {
    setPartialPaintValid(false);
  }

  void layout() {
  }

  /**
   * Render the window onto the screen
   */
  void render(boolean partial) {
    var r = Render.prepare(this, partial);

    var totalBounds = totalBounds();
    if (!partial)
      r.clearRect(totalBounds);
    int btype = mFlags & FLG_BORDER;
    if (btype != BORDER_NONE) {
      if (!partial)
        r.drawRect(totalBounds, btype);
      r.setClipBounds(calcContentBounds());
    }
    paint();
    r = Render.unprepare();
  }

  IRect calcContentBounds() {
    var g = totalBounds();
    int btype = mFlags & FLG_BORDER;
    if (btype != BORDER_NONE) {
      g = g.withInset(2, 1);
    }
    return g;
  }

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
  private static final int FLG_PARTIALPAINTVALID = 1 << 4;
  private IRect mWindowBounds;
  private List<JWindow> mChildren = arrayList();

}
