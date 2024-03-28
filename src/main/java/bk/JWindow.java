package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.List;

import js.base.BaseObject;
import js.geometry.IRect;

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

  List<JWindow> children() {
    return mChildren;
  }

  void setLayoutBounds(IRect bounds) {
    mLayoutBounds = bounds;
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
  void render() {
    var r = Render.SHARED_INSTANCE;
    r.prepare(this);

    var layoutBounds = layoutBounds();
    var clipBounds = layoutBounds;
    r.clearRect(layoutBounds);
    int btype = mFlags & FLG_BORDER;

    if (btype != BORDER_NONE) {
      if (alert("experimenting with focus")) {
        if (hasFocus()) {
          btype = BORDER_THICK;
        } else
          btype = BORDER_THIN;
      }
      r.drawRect(layoutBounds, btype);
      // Now set the clip bounds to exclude the border
      // We inset an extra character horizontally
      clipBounds = clipBounds.withInset(2, 1);
      r.setClipBounds(clipBounds);
    }
    handler().paint();
    r.unprepare();
  }

  /**
   * Let client perform partial rendering of the window
   */
  void renderPartial() {
    var r = Render.SHARED_INSTANCE;
    r.prepare(this);

    var layoutBounds = layoutBounds();
    var clipBounds = layoutBounds;
    int btype = mFlags & FLG_BORDER;
    if (btype != BORDER_NONE) {
      clipBounds = clipBounds.withInset(2, 1);
      r.setClipBounds(clipBounds);
    }
    handler().paintPartial();
    r.unprepare();
  }

  public final boolean hasFocus() {
    return winMgr().focusWindow() == this;
  }

  private IRect mLayoutBounds;
  private List<JWindow> mChildren = arrayList();

  private static final WindowHandler DEFAULT_HANDLER = new WindowHandler() {
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
  private static final int FLG_PARTIALPAINTVALID = 1 << 4;

  private static int sUniqueId = 100;
  private int mId;

  public int id() {
    return mId;
  }

}
