package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import java.util.Stack;

import com.googlecode.lanterna.Symbols;
import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.TextGraphics;
import com.googlecode.lanterna.screen.Screen;

import js.geometry.IPoint;
import js.geometry.IRect;
import js.geometry.MyMath;

public final class Render {

  public static final Render SHARED_INSTANCE = new Render();

  private Render() {
  }

  public IRect clipBounds() {
    return mClipBounds;
  }

  public Render setClipBounds(IRect r) {
    mClipBounds = r;
    return this;
  }

  public JWindow window() {
    return mWindow;
  }

  public Render clearRow(int y, char character) {
    var c = mClipBounds;
    return clearRect(new IRect(c.x, y, c.width, 1), character);
  }

  public Render clearRect(IRect bounds, char character) {
    var p = clampToClip(bounds);
    if (!p.isDegenerate()) {
      mTextGraphics.fillRectangle(new TerminalPosition(p.x, p.y), new TerminalSize(p.width, p.height),
          character);
    }
    return this;
  }

  public Render clearRect(IRect bounds) {
    return clearRect(bounds, ' ');
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

  public Render drawString(int x, int y, int maxLength, String s) {
    do {
      var b = clipBounds();

      // Determine which substring is within the window bounds
      if (y < b.y || y >= b.endY())
        break;
      var x1 = x;
      var x2 = x + s.length();
      x1 = MyMath.clamp(x1, b.x, b.endX());
      x2 = MyMath.clamp(x2, b.x, b.endX());
      if (x1 >= x2)
        break;

      int sStart = x1 - x;
      int sEnd = Math.min(maxLength, x2 - x);
      if (sEnd <= sStart)
        break;
      var tg = textGraphics();
      tg.putString(x1, y, s.substring(sStart, sEnd));
    } while (false);
    return this;
  }

  public Render drawRect(IRect bounds, int type) {
    do {
      checkArgument(type >= 1 && type < BORDER_TOTAL, "unsupported border type:", type);
      int ci = (type - 1) * 6;
      var p = clampToClip(bounds);
      if (p.width < 2 || p.height < 2)
        break;
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
    } while (false);
    return this;
  }

  private TextGraphics textGraphics() {
    return mTextGraphics;
  }

  /**
   * Prepare for subsequent operations to occur with a particular window
   */
  void prepare(JWindow window, boolean partial) {
    mScreen = screen().screen();
    mWindow = window;
    mLayoutBounds = window.layoutBounds();
    mClipBounds = mLayoutBounds;
    mStack = new Stack<>();
    mTextGraphics = mScreen.newTextGraphics();
    mPartial = partial;
  }

  void unprepare() {
    if (!mStack.isEmpty())
      alert("Render.stack isn't empty");
    mStack = null;
    mScreen = null;
    mWindow = null;
    mLayoutBounds = null;
    mClipBounds = null;
    mTextGraphics = null;
  }

  /**
   * Clamp a point to be within the bounds of the window
   */
  private IPoint clampToClip(int wx, int wy) {
    var cx1 = MyMath.clamp(wx, mClipBounds.x, mClipBounds.endX());
    var cy1 = MyMath.clamp(wy, mClipBounds.y, mClipBounds.endY());
    return new IPoint(cx1, cy1);
  }

  private IRect clampToClip(IRect r) {
    var p1 = clampToClip(r.x, r.y);
    var p2 = clampToClip(r.endX(), r.endY());
    return IRect.rectContainingPoints(p1, p2);
  }

  public Render pushStyle(int style) {
    checkArgument(style >= 0 && style < STYLE_TOTAL);
    mStack.push(mTextGraphics);
    var t = mScreen.newTextGraphics();
    switch (style) {
    case STYLE_INVERSE:
      t.setForegroundColor(TextColor.ANSI.WHITE_BRIGHT);
      t.setBackgroundColor(TextColor.ANSI.BLACK);
      break;
    }
    mTextGraphics = t;
    return this;
  }

  public Render pop() {
    mTextGraphics = mStack.pop();
    return this;
  }

  public boolean partial() {
    return mPartial;
  }

  public boolean hasFocus() {
    return window().hasFocus();
  }

  private Stack<TextGraphics> mStack = new Stack<>();
  private IRect mLayoutBounds;
  private IRect mClipBounds;
  private JWindow mWindow;
  private Screen mScreen;
  private TextGraphics mTextGraphics;
  private boolean mPartial;

}
