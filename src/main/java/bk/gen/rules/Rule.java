package bk.gen.rules;

import java.util.Arrays;
import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;

public class Rule implements AbstractData {

  public int[] accounts() {
    return mAccounts;
  }

  public List<String> conditions() {
    return mConditions;
  }

  public List<JSMap> actions() {
    return mActions;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "accounts";
  protected static final String _1 = "conditions";
  protected static final String _2 = "actions";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, DataUtil.encodeBase64Maybe(mAccounts));
    {
      JSList j = new JSList();
      for (String x : mConditions)
        j.add(x);
      m.put(_1, j);
    }
    {
      JSList j = new JSList();
      for (JSMap x : mActions)
        j.add(x);
      m.put(_2, j);
    }
    return m;
  }

  @Override
  public Rule build() {
    return this;
  }

  @Override
  public Rule parse(Object obj) {
    return new Rule((JSMap) obj);
  }

  private Rule(JSMap m) {
    {
      mAccounts = DataUtil.EMPTY_INT_ARRAY;
      Object x = m.optUnsafe(_0);
      if (x != null) {
        mAccounts = DataUtil.parseIntsFromArrayOrBase64(x);
      }
    }
    mConditions = DataUtil.parseListOfObjects(m.optJSList(_1), false);
    mActions = DataUtil.parseListOfObjects(m.optJSList(_2), false);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof Rule))
      return false;
    Rule other = (Rule) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(Arrays.equals(mAccounts, other.mAccounts)))
      return false;
    if (!(mConditions.equals(other.mConditions)))
      return false;
    if (!(mActions.equals(other.mActions)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + Arrays.hashCode(mAccounts);
      for (String x : mConditions)
        if (x != null)
          r = r * 37 + x.hashCode();
      for (JSMap x : mActions)
        if (x != null)
          r = r * 37 + x.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected int[] mAccounts;
  protected List<String> mConditions;
  protected List<JSMap> mActions;
  protected int m__hashcode;

  public static final class Builder extends Rule {

    private Builder(Rule m) {
      mAccounts = m.mAccounts;
      mConditions = DataUtil.mutableCopyOf(m.mConditions);
      mActions = DataUtil.mutableCopyOf(m.mActions);
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
    public Rule build() {
      Rule r = new Rule();
      r.mAccounts = mAccounts;
      r.mConditions = DataUtil.immutableCopyOf(mConditions);
      r.mActions = DataUtil.immutableCopyOf(mActions);
      return r;
    }

    public Builder accounts(int[] x) {
      mAccounts = (x == null) ? DataUtil.EMPTY_INT_ARRAY : x;
      return this;
    }

    public Builder conditions(List<String> x) {
      mConditions = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

    public Builder actions(List<JSMap> x) {
      mActions = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

  }

  public static final Rule DEFAULT_INSTANCE = new Rule();

  private Rule() {
    mAccounts = DataUtil.EMPTY_INT_ARRAY;
    mConditions = DataUtil.emptyList();
    mActions = DataUtil.emptyList();
  }

}
