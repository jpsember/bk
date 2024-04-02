package bk.gen;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSMap;

public class Database implements AbstractData {

  public Map<Integer, String> accounts() {
    return mAccounts;
  }

  public Map<Long, Transaction> transactions() {
    return mTransactions;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "accounts";
  protected static final String _1 = "transactions";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    {
      JSMap j = new JSMap();
      for (Map.Entry<Integer, String> e : mAccounts.entrySet())
        j.put(e.getKey().toString(), e.getValue());
      m.put(_0, j);
    }
    {
      JSMap j = new JSMap();
      for (Map.Entry<Long, Transaction> e : mTransactions.entrySet())
        j.put(e.getKey().toString(), e.getValue().toJson());
      m.put(_1, j);
    }
    return m;
  }

  @Override
  public Database build() {
    return this;
  }

  @Override
  public Database parse(Object obj) {
    return new Database((JSMap) obj);
  }

  private Database(JSMap m) {
    {
      mAccounts = DataUtil.emptyMap();
      {
        JSMap m2 = m.optJSMap("accounts");
        if (m2 != null && !m2.isEmpty()) {
          Map<Integer, String> mp = new ConcurrentHashMap<>();
          for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())
            mp.put(Integer.parseInt(e.getKey()), (String) e.getValue());
          mAccounts = mp;
        }
      }
    }
    {
      mTransactions = DataUtil.emptyMap();
      {
        JSMap m2 = m.optJSMap("transactions");
        if (m2 != null && !m2.isEmpty()) {
          Map<Long, Transaction> mp = new ConcurrentHashMap<>();
          for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())
            mp.put(Long.parseLong(e.getKey()), Transaction.DEFAULT_INSTANCE.parse((JSMap) e.getValue()));
          mTransactions = mp;
        }
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
    if (object == null || !(object instanceof Database))
      return false;
    Database other = (Database) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mAccounts.equals(other.mAccounts)))
      return false;
    if (!(mTransactions.equals(other.mTransactions)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mAccounts.hashCode();
      r = r * 37 + mTransactions.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected Map<Integer, String> mAccounts;
  protected Map<Long, Transaction> mTransactions;
  protected int m__hashcode;

  public static final class Builder extends Database {

    private Builder(Database m) {
      mAccounts = DataUtil.mutableCopyOf(m.mAccounts);
      mTransactions = DataUtil.mutableCopyOf(m.mTransactions);
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
    public Database build() {
      Database r = new Database();
      r.mAccounts = DataUtil.immutableCopyOf(mAccounts);
      r.mTransactions = DataUtil.immutableCopyOf(mTransactions);
      return r;
    }

    public Builder accounts(Map<Integer, String> x) {
      mAccounts = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyMap() : x);
      return this;
    }

    public Builder transactions(Map<Long, Transaction> x) {
      mTransactions = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyMap() : x);
      return this;
    }

  }

  public static final Database DEFAULT_INSTANCE = new Database();

  private Database() {
    mAccounts = DataUtil.emptyMap();
    mTransactions = DataUtil.emptyMap();
  }

}
