package bk.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class Column implements AbstractData {

  public String name() {
    return mName;
  }

  public Alignment alignment() {
    return mAlignment;
  }

  public int width() {
    return mWidth;
  }

  public Datatype datatype() {
    return mDatatype;
  }

  public int growPct() {
    return mGrowPct;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "name";
  protected static final String _1 = "alignment";
  protected static final String _2 = "width";
  protected static final String _3 = "datatype";
  protected static final String _4 = "grow_pct";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mName);
    m.putUnsafe(_1, mAlignment.toString().toLowerCase());
    m.putUnsafe(_2, mWidth);
    m.putUnsafe(_3, mDatatype.toString().toLowerCase());
    m.putUnsafe(_4, mGrowPct);
    return m;
  }

  @Override
  public Column build() {
    return this;
  }

  @Override
  public Column parse(Object obj) {
    return new Column((JSMap) obj);
  }

  private Column(JSMap m) {
    mName = m.opt(_0, "");
    {
      String x = m.opt(_1, "");
      mAlignment = x.isEmpty() ? Alignment.DEFAULT_INSTANCE : Alignment.valueOf(x.toUpperCase());
    }
    mWidth = m.opt(_2, 0);
    {
      String x = m.opt(_3, "");
      mDatatype = x.isEmpty() ? Datatype.DEFAULT_INSTANCE : Datatype.valueOf(x.toUpperCase());
    }
    mGrowPct = m.opt(_4, 0);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof Column))
      return false;
    Column other = (Column) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mName.equals(other.mName)))
      return false;
    if (!(mAlignment.equals(other.mAlignment)))
      return false;
    if (!(mWidth == other.mWidth))
      return false;
    if (!(mDatatype.equals(other.mDatatype)))
      return false;
    if (!(mGrowPct == other.mGrowPct))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mName.hashCode();
      r = r * 37 + mAlignment.ordinal();
      r = r * 37 + mWidth;
      r = r * 37 + mDatatype.ordinal();
      r = r * 37 + mGrowPct;
      m__hashcode = r;
    }
    return r;
  }

  protected String mName;
  protected Alignment mAlignment;
  protected int mWidth;
  protected Datatype mDatatype;
  protected int mGrowPct;
  protected int m__hashcode;

  public static final class Builder extends Column {

    private Builder(Column m) {
      mName = m.mName;
      mAlignment = m.mAlignment;
      mWidth = m.mWidth;
      mDatatype = m.mDatatype;
      mGrowPct = m.mGrowPct;
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
    public Column build() {
      Column r = new Column();
      r.mName = mName;
      r.mAlignment = mAlignment;
      r.mWidth = mWidth;
      r.mDatatype = mDatatype;
      r.mGrowPct = mGrowPct;
      return r;
    }

    public Builder name(String x) {
      mName = (x == null) ? "" : x;
      return this;
    }

    public Builder alignment(Alignment x) {
      mAlignment = (x == null) ? Alignment.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder width(int x) {
      mWidth = x;
      return this;
    }

    public Builder datatype(Datatype x) {
      mDatatype = (x == null) ? Datatype.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder growPct(int x) {
      mGrowPct = x;
      return this;
    }

  }

  public static final Column DEFAULT_INSTANCE = new Column();

  private Column() {
    mName = "";
    mAlignment = Alignment.DEFAULT_INSTANCE;
    mDatatype = Datatype.DEFAULT_INSTANCE;
  }

}
