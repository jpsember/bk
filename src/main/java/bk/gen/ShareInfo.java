package bk.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class ShareInfo implements AbstractData {

  public ShareAction action() {
    return mAction;
  }

  public double shares() {
    return mShares;
  }

  public double bookValue() {
    return mBookValue;
  }

  public String notes() {
    return mNotes;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "action";
  protected static final String _1 = "shares";
  protected static final String _2 = "book_value";
  protected static final String _3 = "notes";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mAction.toString().toLowerCase());
    m.putUnsafe(_1, mShares);
    m.putUnsafe(_2, mBookValue);
    m.putUnsafe(_3, mNotes);
    return m;
  }

  @Override
  public ShareInfo build() {
    return this;
  }

  @Override
  public ShareInfo parse(Object obj) {
    return new ShareInfo((JSMap) obj);
  }

  private ShareInfo(JSMap m) {
    {
      String x = m.opt(_0, "");
      mAction = x.isEmpty() ? ShareAction.DEFAULT_INSTANCE : ShareAction.valueOf(x.toUpperCase());
    }
    mShares = m.opt(_1, 0.0);
    mBookValue = m.opt(_2, 0.0);
    mNotes = m.opt(_3, "");
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof ShareInfo))
      return false;
    ShareInfo other = (ShareInfo) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mAction.equals(other.mAction)))
      return false;
    if (!(mShares == other.mShares))
      return false;
    if (!(mBookValue == other.mBookValue))
      return false;
    if (!(mNotes.equals(other.mNotes)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mAction.ordinal();
      r = r * 37 + (int) mShares;
      r = r * 37 + (int) mBookValue;
      r = r * 37 + mNotes.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected ShareAction mAction;
  protected double mShares;
  protected double mBookValue;
  protected String mNotes;
  protected int m__hashcode;

  public static final class Builder extends ShareInfo {

    private Builder(ShareInfo m) {
      mAction = m.mAction;
      mShares = m.mShares;
      mBookValue = m.mBookValue;
      mNotes = m.mNotes;
    }

    @Override
    public Builder toBuilder() {
      return this;
    }

    @Override
    public int hashCode() {
      m__hashcode = 0;
      return super.hashCode();
    }

    @Override
    public ShareInfo build() {
      ShareInfo r = new ShareInfo();
      r.mAction = mAction;
      r.mShares = mShares;
      r.mBookValue = mBookValue;
      r.mNotes = mNotes;
      return r;
    }

    public Builder action(ShareAction x) {
      mAction = (x == null) ? ShareAction.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder shares(double x) {
      mShares = x;
      return this;
    }

    public Builder bookValue(double x) {
      mBookValue = x;
      return this;
    }

    public Builder notes(String x) {
      mNotes = (x == null) ? "" : x;
      return this;
    }

  }

  public static final ShareInfo DEFAULT_INSTANCE = new ShareInfo();

  private ShareInfo() {
    mAction = ShareAction.DEFAULT_INSTANCE;
    mNotes = "";
  }

}
