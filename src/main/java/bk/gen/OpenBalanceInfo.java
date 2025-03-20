package bk.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class OpenBalanceInfo implements AbstractData {

  public long balance() {
    return mBalance;
  }

  public String comment() {
    return mComment;
  }

  public ShareCalc shareCalc() {
    return mShareCalc;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "balance";
  protected static final String _1 = "comment";
  protected static final String _2 = "share_calc";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mBalance);
    m.putUnsafe(_1, mComment);
    m.putUnsafe(_2, mShareCalc.toJson());
    return m;
  }

  @Override
  public OpenBalanceInfo build() {
    return this;
  }

  @Override
  public OpenBalanceInfo parse(Object obj) {
    return new OpenBalanceInfo((JSMap) obj);
  }

  private OpenBalanceInfo(JSMap m) {
    mBalance = m.opt(_0, 0L);
    mComment = m.opt(_1, "");
    {
      mShareCalc = ShareCalc.DEFAULT_INSTANCE;
      Object x = m.optUnsafe(_2);
      if (x != null) {
        mShareCalc = ShareCalc.DEFAULT_INSTANCE.parse(x);
      }
    }
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof OpenBalanceInfo))
      return false;
    OpenBalanceInfo other = (OpenBalanceInfo) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mBalance == other.mBalance))
      return false;
    if (!(mComment.equals(other.mComment)))
      return false;
    if (!(mShareCalc.equals(other.mShareCalc)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (int)mBalance;
      r = r * 37 + mComment.hashCode();
      r = r * 37 + mShareCalc.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected long mBalance;
  protected String mComment;
  protected ShareCalc mShareCalc;
  protected int m__hashcode;

  public static final class Builder extends OpenBalanceInfo {

    private Builder(OpenBalanceInfo m) {
      mBalance = m.mBalance;
      mComment = m.mComment;
      mShareCalc = m.mShareCalc;
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
    public OpenBalanceInfo build() {
      OpenBalanceInfo r = new OpenBalanceInfo();
      r.mBalance = mBalance;
      r.mComment = mComment;
      r.mShareCalc = mShareCalc;
      return r;
    }

    public Builder balance(long x) {
      mBalance = x;
      return this;
    }

    public Builder comment(String x) {
      mComment = (x == null) ? "" : x;
      return this;
    }

    public Builder shareCalc(ShareCalc x) {
      mShareCalc = (x == null) ? ShareCalc.DEFAULT_INSTANCE : x.build();
      return this;
    }

  }

  public static final OpenBalanceInfo DEFAULT_INSTANCE = new OpenBalanceInfo();

  private OpenBalanceInfo() {
    mComment = "";
    mShareCalc = ShareCalc.DEFAULT_INSTANCE;
  }

}
