package bk.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class LedgerEntry implements AbstractData {

  public int timestamp() {
    return mTimestamp;
  }

  public int accountNumber() {
    return mAccountNumber;
  }

  public int debit() {
    return mDebit;
  }

  public int credit() {
    return mCredit;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "timestamp";
  protected static final String _1 = "account_number";
  protected static final String _2 = "debit";
  protected static final String _3 = "credit";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mTimestamp);
    m.putUnsafe(_1, mAccountNumber);
    m.putUnsafe(_2, mDebit);
    m.putUnsafe(_3, mCredit);
    return m;
  }

  @Override
  public LedgerEntry build() {
    return this;
  }

  @Override
  public LedgerEntry parse(Object obj) {
    return new LedgerEntry((JSMap) obj);
  }

  private LedgerEntry(JSMap m) {
    mTimestamp = m.opt(_0, 0);
    mAccountNumber = m.opt(_1, 0);
    mDebit = m.opt(_2, 0);
    mCredit = m.opt(_3, 0);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof LedgerEntry))
      return false;
    LedgerEntry other = (LedgerEntry) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mTimestamp == other.mTimestamp))
      return false;
    if (!(mAccountNumber == other.mAccountNumber))
      return false;
    if (!(mDebit == other.mDebit))
      return false;
    if (!(mCredit == other.mCredit))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mTimestamp;
      r = r * 37 + mAccountNumber;
      r = r * 37 + mDebit;
      r = r * 37 + mCredit;
      m__hashcode = r;
    }
    return r;
  }

  protected int mTimestamp;
  protected int mAccountNumber;
  protected int mDebit;
  protected int mCredit;
  protected int m__hashcode;

  public static final class Builder extends LedgerEntry {

    private Builder(LedgerEntry m) {
      mTimestamp = m.mTimestamp;
      mAccountNumber = m.mAccountNumber;
      mDebit = m.mDebit;
      mCredit = m.mCredit;
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
    public LedgerEntry build() {
      LedgerEntry r = new LedgerEntry();
      r.mTimestamp = mTimestamp;
      r.mAccountNumber = mAccountNumber;
      r.mDebit = mDebit;
      r.mCredit = mCredit;
      return r;
    }

    public Builder timestamp(int x) {
      mTimestamp = x;
      return this;
    }

    public Builder accountNumber(int x) {
      mAccountNumber = x;
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

  }

  public static final LedgerEntry DEFAULT_INSTANCE = new LedgerEntry();

  private LedgerEntry() {
  }

}
