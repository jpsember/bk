package bk;

import static js.base.Tools.*;

import java.util.List;

import bk.gen.Alignment;
import js.base.BaseObject;
import js.geometry.MyMath;

public class PrintCol extends BaseObject {

  public void add(Object msg) {
    checkState(mMaxLength >= 0, "no max length given!");
    var s = "";
    if (msg != null)
      s = msg.toString();
    mText.add(s);
    var r = Math.min(mMaxLength, s.length());
    if (r > mLengthRequired)
      mLengthRequired = r;
  }

  public void setSeparator(String sep) {
    mSeparator = sep;
  }

  public void setMinLength(int minLength) {
    mMinLength = minLength;
  }

  public void setAlignment(Alignment a) {
    mAlignment = a;
  }

  public void setMaxLength(int maxLength) {
    mMaxLength = maxLength;
  }

  public int slackWidth() {
    return mMaxLength - mMaxTextLength;
  }

  public int requiredLength() {
    return mLengthRequired;
  }

  public void adjustWidth(int numChars) {
    mLengthRequired = MyMath.clamp(mLengthRequired + numChars, mMinLength, mMaxLength);
  }

  public List<String> mText = arrayList();
  private int mMaxTextLength;
  private String mSeparator = " | ";
  private int mMinLength = 0;
  private int mMaxLength = -1;
  public int mLengthRequired;
  public Alignment mAlignment = Alignment.LEFT;

  public int mStretchPct = 10;
  public int mShrinkPct = 10;

}
