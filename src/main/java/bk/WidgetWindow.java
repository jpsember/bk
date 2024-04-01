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

  public WidgetWindow validator(Validator v) {
    checkNotNull(v, "validator");
    mValidator = v;
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
  public void loseFocus() {
    String c = mValidator.validate(mContent);
    c = nullToEmpty(c);
    mContent = c;
    if (c.isEmpty())
      todo("handle a failed validation");
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
    var valueWidth = mWidth; // - SEP - labelWidth;

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
          s = truncate(s, mWidth);
        }

        if (hf)
          screen().setCursorPosition(lx + curPos, ly);
      }
      r.pushStyle(style);
      r.drawString(lx, ly, valueWidth, s);
      r.pop();
    }
    r.pop();
  }

  @Override
  public void processKeyStroke(KeyStroke k) {
    var m = winMgr();
    //pr("keyType:", k.getKeyType(), k);
    todo("have validation, maybe clear if illegal?");
    switch (k.getKeyType()) {
    case ArrowDown:
    case Tab:
      m.moveFocus(1);
      break;
    case ArrowUp:
      m.moveFocus(-1);
      break;
    case ArrowLeft:
      if (mCursorPos == 0)
        break;
      if (mCursorPos > 0)
        mCursorPos--;
      else
        mCursorPos = mContent.length() - 1;
      break;
    case ArrowRight:
      if (mCursorPos < mContent.length())
        mCursorPos++;
      break;
    case Backspace:
      if (mCursorPos > 0) {
        mContent = mContent.substring(0, mCursorPos - 1) + mContent.substring(mCursorPos);
        mCursorPos--;
      } else {
        mCursorPos = 0;
        mContent = "";
      }
      break;
    case Delete:
      if (mCursorPos < 0) {
        mContent = "";
        mCursorPos = 0;
      } else {
        if (mCursorPos < mContent.length())
          mContent = mContent.substring(0, mCursorPos) + mContent.substring(mCursorPos + 1);
      }
      break;
    case Home:
      mCursorPos = 0;
      break;
    case End:
      mCursorPos = mContent.length();
      break;
    case Character: {
      var c = k.getCharacter();
      insertChar(c);
    }
      break;
    default:
      todo("have some sort of fallback");
      break;
    }
    mContent = truncate(mContent, mWidth);
    mCursorPos = MyMath.clamp(mCursorPos, -1, mWidth);
    repaint();
  }

  private void insertChar(char c) {
    if (mCursorPos < 0) {
      mContent = "";
      mCursorPos = 0;
    }
    mContent = mContent.substring(0, mCursorPos) + Character.toString(c) + mContent.substring(mCursorPos);
    mCursorPos++;
  }

  private static String truncate(String s, int maxWidth) {
    if (s.length() > maxWidth)
      return s.substring(0, maxWidth);
    return s;
  }

  private int mCursorPos = -1; // position of cursor, or -1 if entire string is highlighted
  private String mContent = "";

  private int mWidth = 16;
  private String mLabel = "<no label!>";
  private Validator mValidator = DEFAULT_VALIDATOR;
}
