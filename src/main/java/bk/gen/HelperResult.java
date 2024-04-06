package bk.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class HelperResult implements AbstractData {

  public boolean selected() {
    return mSelected;
  }

  public String text() {
    return mText;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "selected";
  protected static final String _1 = "text";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mSelected);
    m.putUnsafe(_1, mText);
    return m;
  }

  @Override
  public HelperResult build() {
    return this;
  }

  @Override
  public HelperResult parse(Object obj) {
    return new HelperResult((JSMap) obj);
  }

  private HelperResult(JSMap m) {
    mSelected = m.opt(_0, false);
    mText = m.opt(_1, "");
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof HelperResult))
      return false;
    HelperResult other = (HelperResult) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mSelected == other.mSelected))
      return false;
    if (!(mText.equals(other.mText)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (mSelected ? 1 : 0);
      r = r * 37 + mText.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected boolean mSelected;
  protected String mText;
  protected int m__hashcode;

  public static final class Builder extends HelperResult {

    private Builder(HelperResult m) {
      mSelected = m.mSelected;
      mText = m.mText;
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
    public HelperResult build() {
      HelperResult r = new HelperResult();
      r.mSelected = mSelected;
      r.mText = mText;
      return r;
    }

    public Builder selected(boolean x) {
      mSelected = x;
      return this;
    }

    public Builder text(String x) {
      mText = (x == null) ? "" : x;
      return this;
    }

  }

  public static final HelperResult DEFAULT_INSTANCE = new HelperResult();

  private HelperResult() {
    mText = "";
  }

}
