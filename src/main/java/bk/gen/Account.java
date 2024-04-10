package bk.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class Account implements AbstractData {

  public int number() {
    return mNumber;
  }

  public String name() {
    return mName;
  }

  public long balance() {
    return mBalance;
  }

  public long budget() {
    return mBudget;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "number";
  protected static final String _1 = "name";
  protected static final String _2 = "balance";
  protected static final String _3 = "budget";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mNumber);
    m.putUnsafe(_1, mName);
    m.putUnsafe(_2, mBalance);
    m.putUnsafe(_3, mBudget);
    return m;
  }

  @Override
  public Account build() {
    return this;
  }

  @Override
  public Account parse(Object obj) {
    return new Account((JSMap) obj);
  }

  private Account(JSMap m) {
    mNumber = m.opt(_0, 0);
    mName = m.opt(_1, "");
    mBalance = m.opt(_2, 0L);
    mBudget = m.opt(_3, 0L);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof Account))
      return false;
    Account other = (Account) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mNumber == other.mNumber))
      return false;
    if (!(mName.equals(other.mName)))
      return false;
    if (!(mBalance == other.mBalance))
      return false;
    if (!(mBudget == other.mBudget))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mNumber;
      r = r * 37 + mName.hashCode();
      r = r * 37 + (int)mBalance;
      r = r * 37 + (int)mBudget;
      m__hashcode = r;
    }
    return r;
  }

  protected int mNumber;
  protected String mName;
  protected long mBalance;
  protected long mBudget;
  protected int m__hashcode;

  public static final class Builder extends Account {

    private Builder(Account m) {
      mNumber = m.mNumber;
      mName = m.mName;
      mBalance = m.mBalance;
      mBudget = m.mBudget;
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
    public Account build() {
      Account r = new Account();
      r.mNumber = mNumber;
      r.mName = mName;
      r.mBalance = mBalance;
      r.mBudget = mBudget;
      return r;
    }

    public Builder number(int x) {
      mNumber = x;
      return this;
    }

    public Builder name(String x) {
      mName = (x == null) ? "" : x;
      return this;
    }

    public Builder balance(long x) {
      mBalance = x;
      return this;
    }

    public Builder budget(long x) {
      mBudget = x;
      return this;
    }

  }

  public static final Account DEFAULT_INSTANCE = new Account();

  private Account() {
    mName = "";
  }

}
