package bk.gen;

import js.data.AbstractData;
import js.json.JSMap;

public class UndoEntry implements AbstractData {

  public boolean insert() {
    return mInsert;
  }

  public Account account() {
    return mAccount;
  }

  public Transaction transaction() {
    return mTransaction;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "insert";
  protected static final String _1 = "account";
  protected static final String _2 = "transaction";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mInsert);
    if (mAccount != null) {
      m.putUnsafe(_1, mAccount.toJson());
    }
    if (mTransaction != null) {
      m.putUnsafe(_2, mTransaction.toJson());
    }
    return m;
  }

  @Override
  public UndoEntry build() {
    return this;
  }

  @Override
  public UndoEntry parse(Object obj) {
    return new UndoEntry((JSMap) obj);
  }

  private UndoEntry(JSMap m) {
    mInsert = m.opt(_0, false);
    {
      Object x = m.optUnsafe(_1);
      if (x != null) {
        mAccount = Account.DEFAULT_INSTANCE.parse(x);
      }
    }
    {
      Object x = m.optUnsafe(_2);
      if (x != null) {
        mTransaction = Transaction.DEFAULT_INSTANCE.parse(x);
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
    if (object == null || !(object instanceof UndoEntry))
      return false;
    UndoEntry other = (UndoEntry) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mInsert == other.mInsert))
      return false;
    if ((mAccount == null) ^ (other.mAccount == null))
      return false;
    if (mAccount != null) {
      if (!(mAccount.equals(other.mAccount)))
        return false;
    }
    if ((mTransaction == null) ^ (other.mTransaction == null))
      return false;
    if (mTransaction != null) {
      if (!(mTransaction.equals(other.mTransaction)))
        return false;
    }
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (mInsert ? 1 : 0);
      if (mAccount != null) {
        r = r * 37 + mAccount.hashCode();
      }
      if (mTransaction != null) {
        r = r * 37 + mTransaction.hashCode();
      }
      m__hashcode = r;
    }
    return r;
  }

  protected boolean mInsert;
  protected Account mAccount;
  protected Transaction mTransaction;
  protected int m__hashcode;

  public static final class Builder extends UndoEntry {

    private Builder(UndoEntry m) {
      mInsert = m.mInsert;
      mAccount = m.mAccount;
      mTransaction = m.mTransaction;
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
    public UndoEntry build() {
      UndoEntry r = new UndoEntry();
      r.mInsert = mInsert;
      r.mAccount = mAccount;
      r.mTransaction = mTransaction;
      return r;
    }

    public Builder insert(boolean x) {
      mInsert = x;
      return this;
    }

    public Builder account(Account x) {
      mAccount = (x == null) ? null : x.build();
      return this;
    }

    public Builder transaction(Transaction x) {
      mTransaction = (x == null) ? null : x.build();
      return this;
    }

  }

  public static final UndoEntry DEFAULT_INSTANCE = new UndoEntry();

  private UndoEntry() {
  }

}
