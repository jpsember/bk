package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComponent;

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

  public boolean layoutValid() {
    return hasFlag(FLG_LAYOUTVALID);
  }

  public void setLayoutInvalid() {
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

  public void layout(IRect boundsWithinScreen) {
    setBounds(boundsWithinScreen);
    setLayoutValid();
    setPaintValid(false);
  }

  /**
   * Render the window onto the screen
   */
  public void render() {
    todo("for now, just drawing a rectangle");
    pr("rendering bounds:", bounds(), "for:", name());
    // Get rectangle with origin at this window's top left
    var b = new IRect(bounds().size());

    clearRect(b.withInset(1));
    drawRect(b);
  }

  /**
   * Translate a point from window space to screen space, and clamp to screen
   * 
   * @param wx
   *          window space coordinates
   * @param wy
   * @return point, x in low word, y in high
   */
  private int translateAndClampToScreen(int wx, int wy) {
    var wb = bounds();
    var sx = wx + wb.x;
    var sy = wy + wb.y;

    var cx1 = clampToWindowBoundsX(sx);
    var cy1 = clampToWindowBoundsY(sy);
    return cx1 + (cy1 << 16);
  }

  public void clearRect(IRect bounds) {
    var coord = translateAndClampToScreen(bounds.x, bounds.y);
    var cx1 = coord & 0xffff;
    var cy1 = coord >> 16;
    coord = translateAndClampToScreen(bounds.endX(), bounds.endY());
    var cx2 = coord & 0xffff;
    var cy2 = coord >> 16;
    if (cx1 >= cx2 || cy1 >= cy2)
      return;
    var js = screen();
    var s = js.screen();

    var tg = s.newTextGraphics();
    tg.fillRectangle(new TerminalPosition(cx1, cy1), new TerminalSize(cx2 - cx1, cy2 - cy1), ' ');
  }

  public void drawRect(IRect bounds) {
    if (false && alert("changing size to 3,3"))
      bounds = new IRect(bounds.x, bounds.y, 3, 3);
    var coord = translateAndClampToScreen(bounds.x, bounds.y);
    var x1 = coord & 0xffff;
    var y1 = coord >> 16;

    coord = translateAndClampToScreen(bounds.endX(), bounds.endY());
    var x2 = coord & 0xffff;
    var y2 = coord >> 16;

    pr("drawRect, bounds:", bounds, "cx1:", x1, "cx2:", x2, "cy1:", y1, "cy2:", y2);

    if (x1 >= x2 || y1 >= y2)
      return;

    var js = screen();
    var s = js.screen();

    var tg = s.newTextGraphics();
    //    var min = new IPoint(cb.x, cb.y); //toTerm(cb.x, cb.y);
    //    var max = toTerm(cb.endX(), cb.endY());
    if (x2 - x1 >= 2) {
      tg.drawLine(x1 + 1, y1, x2 - 2, y1, Symbols.DOUBLE_LINE_HORIZONTAL);
      tg.drawLine(x1 + 1, y2 - 1, x2 - 2, y2 - 1, Symbols.DOUBLE_LINE_HORIZONTAL);
    }
    if (y2 - y1 >= 2) {
      tg.drawLine(x1, y1 + 1, x1, y2 - 2, Symbols.DOUBLE_LINE_VERTICAL);
      tg.drawLine(x2 - 1, y1 + 1, x2 - 1, y2 - 2, Symbols.DOUBLE_LINE_VERTICAL);
    }
    tg.setCharacter(x1, y1, Symbols.DOUBLE_LINE_TOP_LEFT_CORNER);
    tg.setCharacter(x1, y2 - 1, Symbols.DOUBLE_LINE_BOTTOM_LEFT_CORNER);
    tg.setCharacter(x2 - 1, y1, Symbols.DOUBLE_LINE_TOP_RIGHT_CORNER);
    tg.setCharacter(x2 - 1, y2 - 1, Symbols.DOUBLE_LINE_BOTTOM_RIGHT_CORNER);
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

  //  private IRect clipToWindowBounds(IRect bounds) {
  //    var min = clampToWindowBounds(bounds.bottomLeft());
  //    var max = clampToWindowBounds(bounds.topRight());
  //    return new IRect(min, max);
  //  }
  //
  //  private IRect clipToWindowBounds(int x, int y, int w, int h) {
  //    var x1 = clampToWindowBoundsX(x);
  //    var min = clampToWindowBounds(bounds.bottomLeft());
  //    var max = clampToWindowBounds(bounds.topRight());
  //    return new IRect(min, max);
  //  }

  private int clampToWindowBoundsX(int x) {
    return MyMath.clamp(x, mBounds.x, mBounds.endX());
  }

  private int clampToWindowBoundsY(int y) {
    return MyMath.clamp(y, mBounds.y, mBounds.endY());
  }

  private IRect mBounds;
  private List<JWindow> mChildren = arrayList();

  private static final WindowHandler DEFAULT_HANDLER = new WindowHandler() {

    @Override
    public void paint(JWindow window) {

    }
  };
  //
  //  public final JWindow setId(String id) {
  //    checkState(!hasId(), "already has an id");
  //    // If id is not null, it cannot be empty
  //    checkArgument(!"".equals(id));
  //    mId = id;
  //    return this;
  //  }
  //
  //  public final boolean hasId() {
  //    return mId != null;
  //  }
  //
  //  public final String id() {
  //    if (mId == null)
  //      throw badState("window has no id");
  //    return mId;
  //  }
  //
  //  public final String optionalId() {
  //    return ifNullOrEmpty(mId, "<no id>");
  //  }
  //
  //  private String mId;

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
  //
  //  @Override
  //  public String toString() {
  //    return id() + ":" + getClass().getSimpleName();
  //  }

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

  private int mFlags;
  private static final int FLG_PAINTVALID = 1 << 0;
  private static final int FLG_LAYOUTVALID = 1 << 1;
  private static int sUniqueId = 100;
  private int mId;
}
