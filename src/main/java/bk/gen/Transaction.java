package bk.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class Transaction implements AbstractData {

  public int date() {
    return mDate;
  }

  public int amount() {
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

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "date";
  protected static final String _1 = "amount";
  protected static final String _2 = "debit";
  protected static final String _3 = "credit";
  protected static final String _4 = "description";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mDate);
    m.putUnsafe(_1, mAmount);
    m.putUnsafe(_2, mDebit);
    m.putUnsafe(_3, mCredit);
    m.putUnsafe(_4, mDescription);
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
    mDate = m.opt(_0, 0);
    mAmount = m.opt(_1, 0);
    mDebit = m.opt(_2, 0);
    mCredit = m.opt(_3, 0);
    mDescription = m.opt(_4, "");
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
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mDate;
      r = r * 37 + mAmount;
      r = r * 37 + mDebit;
      r = r * 37 + mCredit;
      r = r * 37 + mDescription.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected int mDate;
  protected int mAmount;
  protected int mDebit;
  protected int mCredit;
  protected String mDescription;
  protected int m__hashcode;

  public static final class Builder extends Transaction {

    private Builder(Transaction m) {
      mDate = m.mDate;
      mAmount = m.mAmount;
      mDebit = m.mDebit;
      mCredit = m.mCredit;
      mDescription = m.mDescription;
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
      r.mDate = mDate;
      r.mAmount = mAmount;
      r.mDebit = mDebit;
      r.mCredit = mCredit;
      r.mDescription = mDescription;
      return r;
    }

    public Builder date(int x) {
      mDate = x;
      return this;
    }

    public Builder amount(int x) {
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

  }

  public static final Transaction DEFAULT_INSTANCE = new Transaction();

  private Transaction() {
    mDescription = "";
  }

}
