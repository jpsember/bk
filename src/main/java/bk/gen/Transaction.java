package bk.gen;

import java.util.Arrays;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSMap;

public class Transaction implements AbstractData {

  public long timestamp() {
    return mTimestamp;
  }

  public long date() {
    return mDate;
  }

  public long amount() {
    return mAmount;
  }

  public int debit() {
    return mDebit;
  }

  public int credit() {
    return mCredit;
  }

  public String description() {
    return mDescription;
  }

  public long[] children() {
    return mChildren;
  }

  public long parent() {
    return mParent;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "timestamp";
  protected static final String _1 = "date";
  protected static final String _2 = "amount";
  protected static final String _3 = "debit";
  protected static final String _4 = "credit";
  protected static final String _5 = "description";
  protected static final String _6 = "children";
  protected static final String _7 = "parent";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mTimestamp);
    m.putUnsafe(_1, mDate);
    m.putUnsafe(_2, mAmount);
    m.putUnsafe(_3, mDebit);
    m.putUnsafe(_4, mCredit);
    m.putUnsafe(_5, mDescription);
    m.putUnsafe(_6, DataUtil.encodeBase64Maybe(mChildren));
    m.putUnsafe(_7, mParent);
    return m;
  }

  @Override
  public Transaction build() {
    return this;
  }

  @Override
  public Transaction parse(Object obj) {
    return new Transaction((JSMap) obj);
  }

  private Transaction(JSMap m) {
    mTimestamp = m.opt(_0, 0L);
    mDate = m.opt(_1, 0L);
    mAmount = m.opt(_2, 0L);
    mDebit = m.opt(_3, 0);
    mCredit = m.opt(_4, 0);
    mDescription = m.opt(_5, "");
    {
      mChildren = DataUtil.EMPTY_LONG_ARRAY;
      Object x = m.optUnsafe(_6);
      if (x != null) {
        mChildren = DataUtil.parseLongsFromArrayOrBase64(x);
      }
    }
    mParent = m.opt(_7, 0L);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof Transaction))
      return false;
    Transaction other = (Transaction) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mTimestamp == other.mTimestamp))
      return false;
    if (!(mDate == other.mDate))
      return false;
    if (!(mAmount == other.mAmount))
      return false;
    if (!(mDebit == other.mDebit))
      return false;
    if (!(mCredit == other.mCredit))
      return false;
    if (!(mDescription.equals(other.mDescription)))
      return false;
    if (!(Arrays.equals(mChildren, other.mChildren)))
      return false;
    if (!(mParent == other.mParent))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (int)mTimestamp;
      r = r * 37 + (int)mDate;
      r = r * 37 + (int)mAmount;
      r = r * 37 + mDebit;
      r = r * 37 + mCredit;
      r = r * 37 + mDescription.hashCode();
      r = r * 37 + Arrays.hashCode(mChildren);
      r = r * 37 + (int)mParent;
      m__hashcode = r;
    }
    return r;
  }

  protected long mTimestamp;
  protected long mDate;
  protected long mAmount;
  protected int mDebit;
  protected int mCredit;
  protected String mDescription;
  protected long[] mChildren;
  protected long mParent;
  protected int m__hashcode;

  public static final class Builder extends Transaction {

    private Builder(Transaction m) {
      mTimestamp = m.mTimestamp;
      mDate = m.mDate;
      mAmount = m.mAmount;
      mDebit = m.mDebit;
      mCredit = m.mCredit;
      mDescription = m.mDescription;
      mChildren = m.mChildren;
      mParent = m.mParent;
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
    public Transaction build() {
      Transaction r = new Transaction();
      r.mTimestamp = mTimestamp;
      r.mDate = mDate;
      r.mAmount = mAmount;
      r.mDebit = mDebit;
      r.mCredit = mCredit;
      r.mDescription = mDescription;
      r.mChildren = mChildren;
      r.mParent = mParent;
      return r;
    }

    public Builder timestamp(long x) {
      mTimestamp = x;
      return this;
    }

    public Builder date(long x) {
      mDate = x;
      return this;
    }

    public Builder amount(long x) {
      mAmount = x;
      return this;
    }

    public Builder debit(int x) {
      mDebit = x;
      return this;
    }

    public Builder credit(int x) {
      mCredit = x;
      return this;
    }

    public Builder description(String x) {
      mDescription = (x == null) ? "" : x;
      return this;
    }

    public Builder children(long[] x) {
      mChildren = (x == null) ? DataUtil.EMPTY_LONG_ARRAY : x;
      return this;
    }

    public Builder parent(long x) {
      mParent = x;
      return this;
    }

  }

  public static final Transaction DEFAULT_INSTANCE = new Transaction();

  private Transaction() {
    mDescription = "";
    mChildren = DataUtil.EMPTY_LONG_ARRAY;
  }

}
