package bk;

import static bk.Util.*;
import static js.base.Tools.*;

import com.googlecode.lanterna.input.KeyStroke;

import js.geometry.MyMath;

public class WidgetWindow extends JWindow implements FocusHandler {

  public WidgetWindow width(int width) {
    mWidth = width;
    return this;
  }

  public int width() {
    return mWidth;
  }

  public WidgetWindow label(String label) {
    mLabel = label;
    return this;
  }

  @Override
  public void gainFocus() {
    mCursorPos = -1;
  }

  @Override
  public void paint() {
    var r = Render.SHARED_INSTANCE;
    var b = r.clipBounds();
    b = b.withInset(1, 0);
    var SEP = 1;
    var labelWidth = b.width / 2;
    //    var valueWidth = b.width - SEP - labelWidth;

    boolean hf = hasFocus();
    r.pushStyle(hf ? STYLE_INVERSE : STYLE_NORMAL);

    {

      var ef = mLabel + ":";
      r.drawString(b.x + labelWidth - ef.length(), b.y, labelWidth, ef);
    }
    {

      var lx = b.x + labelWidth + SEP;
      var ly = b.y;
      var s = truncate(mContent, mWidth);
      var style = STYLE_NORMAL;
      if (hf) {
        int curPos = mCursorPos;
        if (curPos < 0) {
          // Highlight the entire text
          style = STYLE_INVERSE;
          curPos = s.length();
        } else {
          int i = MyMath.clamp(mCursorPos, 0, s.length());
          s = s.substring(0, i) + " " + s.substring(i);
          s = truncate(s, mWidth);
        }

        if (hf)
          screen().setCursorPosition(lx + curPos, ly);
      }
      r.pushStyle(style);
      r.drawString(lx, ly, mWidth, s);
      r.pop();
    }
    r.pop();
  }

  @Override
  public void processKeyStroke(KeyStroke k) {
    var m = winMgr();
    switch (k.getKeyType()) {
    case ArrowDown:
    case Tab:
      m.moveFocus(1);
      break;
    case ArrowUp:
      m.moveFocus(-1);
      break;
    default:
      todo("have some sort of fallback");
      break;
    }
  }

  private static String truncate(String s, int maxWidth) {
    if (s.length() > maxWidth)
      return s.substring(0, maxWidth);
    return s;
  }

  private int mCursorPos = -1; // position of cursor, or -1 if entire string is highlighted
  private String mContent = "wassup";

  private int mWidth = 16;
  private String mLabel = "<no label!>";

}
