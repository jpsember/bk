package bk.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class ShareCalc implements AbstractData {

  public double shares() {
    return mShares;
  }

  public double bookValue() {
    return mBookValue;
  }

  public double capGain() {
    return mCapGain;
  }

  public String error() {
    return mError;
  }

  public int numTrans() {
    return mNumTrans;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "shares";
  protected static final String _1 = "book_value";
  protected static final String _2 = "cap_gain";
  protected static final String _3 = "error";
  protected static final String _4 = "num_trans";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mShares);
    m.putUnsafe(_1, mBookValue);
    m.putUnsafe(_2, mCapGain);
    m.putUnsafe(_3, mError);
    m.putUnsafe(_4, mNumTrans);
    return m;
  }

  @Override
  public ShareCalc build() {
    return this;
  }

  @Override
  public ShareCalc parse(Object obj) {
    return new ShareCalc((JSMap) obj);
  }

  private ShareCalc(JSMap m) {
    mShares = m.opt(_0, 0.0);
    mBookValue = m.opt(_1, 0.0);
    mCapGain = m.opt(_2, 0.0);
    mError = m.opt(_3, "");
    mNumTrans = m.opt(_4, 0);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof ShareCalc))
      return false;
    ShareCalc other = (ShareCalc) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mShares == other.mShares))
      return false;
    if (!(mBookValue == other.mBookValue))
      return false;
    if (!(mCapGain == other.mCapGain))
      return false;
    if (!(mError.equals(other.mError)))
      return false;
    if (!(mNumTrans == other.mNumTrans))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (int) mShares;
      r = r * 37 + (int) mBookValue;
      r = r * 37 + (int) mCapGain;
      r = r * 37 + mError.hashCode();
      r = r * 37 + mNumTrans;
      m__hashcode = r;
    }
    return r;
  }

  protected double mShares;
  protected double mBookValue;
  protected double mCapGain;
  protected String mError;
  protected int mNumTrans;
  protected int m__hashcode;

  public static final class Builder extends ShareCalc {

    private Builder(ShareCalc m) {
      mShares = m.mShares;
      mBookValue = m.mBookValue;
      mCapGain = m.mCapGain;
      mError = m.mError;
      mNumTrans = m.mNumTrans;
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
    public ShareCalc build() {
      ShareCalc r = new ShareCalc();
      r.mShares = mShares;
      r.mBookValue = mBookValue;
      r.mCapGain = mCapGain;
      r.mError = mError;
      r.mNumTrans = mNumTrans;
      return r;
    }

    public Builder shares(double x) {
      mShares = x;
      return this;
    }

    public Builder bookValue(double x) {
      mBookValue = x;
      return this;
    }

    public Builder capGain(double x) {
      mCapGain = x;
      return this;
    }

    public Builder error(String x) {
      mError = (x == null) ? "" : x;
      return this;
    }

    public Builder numTrans(int x) {
      mNumTrans = x;
      return this;
    }

  }

  public static final ShareCalc DEFAULT_INSTANCE = new ShareCalc();

  private ShareCalc() {
    mError = "";
  }

}
