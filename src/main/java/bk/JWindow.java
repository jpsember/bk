package bk;

import static js.base.Tools.*;

import java.util.List;

import js.geometry.IRect;

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

  private IRect mBounds;
  private List<JWindow> mChildren = arrayList();
  private boolean mPaintValid;

  private static final WindowHandler DEFAULT_HANDLER = new WindowHandler() {

    @Override
    public void paint(JWindow window) {

    }
  };
}
