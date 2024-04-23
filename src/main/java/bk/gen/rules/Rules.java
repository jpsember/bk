package bk.gen.rules;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSMap;

public class Rules implements AbstractData {

  public Map<String, Rule> rules() {
    return mRules;
  }

  public String version() {
    return mVersion;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "rules";
  protected static final String _1 = "version";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    {
      JSMap j = new JSMap();
      for (Map.Entry<String, Rule> e : mRules.entrySet())
        j.put(e.getKey(), e.getValue().toJson());
      m.put(_0, j);
    }
    m.putUnsafe(_1, mVersion);
    return m;
  }

  @Override
  public Rules build() {
    return this;
  }

  @Override
  public Rules parse(Object obj) {
    return new Rules((JSMap) obj);
  }

  private Rules(JSMap m) {
    {
      mRules = DataUtil.emptyMap();
      {
        JSMap m2 = m.optJSMap("rules");
        if (m2 != null && !m2.isEmpty()) {
          Map<String, Rule> mp = new ConcurrentHashMap<>();
          for (Map.Entry<String, Object> e : m2.wrappedMap().entrySet())
            mp.put(e.getKey(), Rule.DEFAULT_INSTANCE.parse((JSMap) e.getValue()));
          mRules = mp;
        }
      }
    }
    mVersion = m.opt(_1, "");
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof Rules))
      return false;
    Rules other = (Rules) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mRules.equals(other.mRules)))
      return false;
    if (!(mVersion.equals(other.mVersion)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mRules.hashCode();
      r = r * 37 + mVersion.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected Map<String, Rule> mRules;
  protected String mVersion;
  protected int m__hashcode;

  public static final class Builder extends Rules {

    private Builder(Rules m) {
      mRules = DataUtil.mutableCopyOf(m.mRules);
      mVersion = m.mVersion;
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
    public Rules build() {
      Rules r = new Rules();
      r.mRules = DataUtil.immutableCopyOf(mRules);
      r.mVersion = mVersion;
      return r;
    }

    public Builder rules(Map<String, Rule> x) {
      mRules = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyMap() : x);
      return this;
    }

    public Builder version(String x) {
      mVersion = (x == null) ? "" : x;
      return this;
    }

  }

  public static final Rules DEFAULT_INSTANCE = new Rules();

  private Rules() {
    mRules = DataUtil.emptyMap();
    mVersion = "";
  }

}
