package bk;

import static js.base.Tools.*;
import static bk.Util.*;

import js.base.BaseObject;
import js.geometry.IPoint;
import js.geometry.MyMath;

public class TextEdit extends BaseObject {

  public TextEdit location(int x, int y) {
    return location(new IPoint(x, y));
  }

  public TextEdit location(IPoint loc) {
    mLocation = loc;
    return this;
  }

  public TextEdit width(int width) {
    mWidth = width;
    return this;
  }

  public TextEdit active(boolean f) {
    mActive = f;
    return this;
  }

  public TextEdit cursor(int cursorPos) {
    mCursorPos = cursorPos;
    return this;
  }

  public TextEdit content(String s) {
    mContent = s;
    todo("when do we update the cursor position?");
    return this;
  }

  public TextEdit handler(TextEditHandler handler) {
    mHandler = nullTo(handler, DEFAULT_HANDLER);
    return this;
  }

  private static final TextEditHandler DEFAULT_HANDLER = new TextEditHandler() {
  };

  private static String truncate(String s, int maxWidth) {
    if (s.length() > maxWidth)
      return s.substring(0, maxWidth);
    return s;
  }

  public void render() {
    checkState(mWidth != 0, "no width for TextEdit!");
    var r = Render.SHARED_INSTANCE;
    var s = truncate(mContent, mWidth);
    boolean inv = false;
    if (mActive) {
      int curPos = mCursorPos;
      if (curPos < 0) {
        // Highlight the entire text
        inv = true;
        r.pushStyle(STYLE_INVERSE);
        curPos = s.length();
      } else {
        int i = MyMath.clamp(mCursorPos, 0, s.length());
        s = s.substring(0, i) + " " + s.substring(i);
        s = truncate(s, mWidth);
      }
      if ( focus() == mHandler)
        screen().setCursorPosition(mLocation.x + curPos, mLocation.y);
    }

    r.drawString(mLocation.x, mLocation.y, mWidth, s);
    if (inv)
      r.pop();
  }

  private IPoint mLocation;
  private int mWidth;
  private TextEditHandler mHandler = DEFAULT_HANDLER;
  private boolean mActive;
  private int mCursorPos = -1; // position of cursor, or -1 if entire string is highlighted
  private String mContent = "";

}
