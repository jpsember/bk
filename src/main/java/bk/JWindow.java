package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComponent;

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

  /**
   * Render the window onto the screen
   */
  public void render() {
    todo("for now, just drawing a rectangle");
    pr("rendering bounds:", bounds());
    drawRect(new IRect(bounds().size()));
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

  public final JWindow setId(String id) {
    checkState(!hasId(), "already has an id");
    // If id is not null, it cannot be empty
    checkArgument(!"".equals(id));
    mId = id;
    return this;
  }

  public final boolean hasId() {
    return mId != null;
  }

  public final String id() {
    if (mId == null)
      throw badState("window has no id");
    return mId;
  }

  public final String optionalId() {
    return ifNullOrEmpty(mId, "<no id>");
  }

  private String mId;

  //  protected final void registerListener(WidgetListener listener) {
  //    mListener = listener;
  //  }

  /**
   * Notify WidgetListener, if there is one, of an event involving this widget
   */
  protected final void notifyListener() {
    notFinished("notifyListener");
    //    if (mListener != null)
    //      widgets().notifyWidgetListener(this, mListener);
  }

  @Override
  public String toString() {
    return id() + ":" + getClass().getSimpleName();
  }

  public void displayKeyboard() {
    throw new UnsupportedOperationException();
  }

  public void hideKeyboard() {
    throw new UnsupportedOperationException();
  }

  public void setInputType(int inputType) {
    throw new UnsupportedOperationException();
  }

  public void setEnabled(boolean enabled) {
    throw notSupported(className());
  }

  public boolean enabled() {
    throw notSupported(className());
  }

  public void setVisible(boolean visible) {
    todo("setVisible not implemented for:", className());
  }

  public void doClick() {
    throw new UnsupportedOperationException();
  }

  public void setChecked(boolean state) {
    throw new UnsupportedOperationException();
  }

  public boolean isChecked() {
    throw new UnsupportedOperationException();
  }

  public void setValue(Number number) {
    throw new UnsupportedOperationException();
  }

  /**
   * Replace this widget in its view hierarchy with another
   */
  public void replaceWith(JWindow other) {
    throw new UnsupportedOperationException();
  }

  public void actionPerformed(ActionEvent e) {
    notifyListener();
  }

  public void setComponent(JComponent component) {
    mWrappedComponent = component;
  }

  public final JComponent component() {
    return mWrappedComponent;
  }

  public final <T extends JComponent> T swingComponent() {
    return (T) component();
  }

  /**
   * Get component to attach tooltip to (if there is one). Default
   * implementation returns swingComponent()
   */
  public JComponent componentForTooltip() {
    return swingComponent();
  }

  public void setText(String text) {
    throw new UnsupportedOperationException();
  }

  public String getText() {
    throw new UnsupportedOperationException();
  }

  public void setHint(String hint) {
    throw new UnsupportedOperationException();
  }

  private String className() {
    return getClass().getSimpleName();
  }

  //  private WidgetListener mListener;
  private JComponent mWrappedComponent;

  void setSize(int sizeExpr) {
    mSizer = sizeExpr;
  }

  int getSizeExpr() {
    checkArgument(mSizer != 0, "size expression must not be zero");
    return mSizer;
  }

  int mSizer;
}
