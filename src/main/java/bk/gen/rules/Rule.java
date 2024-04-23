package bk.gen.rules;

import java.util.Arrays;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSMap;

public class Rule implements AbstractData {

  public int[] accounts() {
    return mAccounts;
  }

  public ActionName action() {
    return mAction;
  }

  public double percent() {
    return mPercent;
  }

  public int targetAccount() {
    return mTargetAccount;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "accounts";
  protected static final String _1 = "action";
  protected static final String _2 = "percent";
  protected static final String _3 = "target_account";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, DataUtil.encodeBase64Maybe(mAccounts));
    m.putUnsafe(_1, mAction.toString().toLowerCase());
    m.putUnsafe(_2, mPercent);
    m.putUnsafe(_3, mTargetAccount);
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
    {
      String x = m.opt(_1, "");
      mAction = x.isEmpty() ? ActionName.DEFAULT_INSTANCE : ActionName.valueOf(x.toUpperCase());
    }
    mPercent = m.opt(_2, 0.0);
    mTargetAccount = m.opt(_3, 0);
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
    if (!(mAction.equals(other.mAction)))
      return false;
    if (!(mPercent == other.mPercent))
      return false;
    if (!(mTargetAccount == other.mTargetAccount))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + Arrays.hashCode(mAccounts);
      r = r * 37 + mAction.ordinal();
      r = r * 37 + (int) mPercent;
      r = r * 37 + mTargetAccount;
      m__hashcode = r;
    }
    return r;
  }

  protected int[] mAccounts;
  protected ActionName mAction;
  protected double mPercent;
  protected int mTargetAccount;
  protected int m__hashcode;

  public static final class Builder extends Rule {

    private Builder(Rule m) {
      mAccounts = m.mAccounts;
      mAction = m.mAction;
      mPercent = m.mPercent;
      mTargetAccount = m.mTargetAccount;
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
      r.mAction = mAction;
      r.mPercent = mPercent;
      r.mTargetAccount = mTargetAccount;
      return r;
    }

    public Builder accounts(int[] x) {
      mAccounts = (x == null) ? DataUtil.EMPTY_INT_ARRAY : x;
      return this;
    }

    public Builder action(ActionName x) {
      mAction = (x == null) ? ActionName.DEFAULT_INSTANCE : x;
      return this;
    }

    public Builder percent(double x) {
      mPercent = x;
      return this;
    }

    public Builder targetAccount(int x) {
      mTargetAccount = x;
      return this;
    }

  }

  public static final Rule DEFAULT_INSTANCE = new Rule();

  private Rule() {
    mAccounts = DataUtil.EMPTY_INT_ARRAY;
    mAction = ActionName.DEFAULT_INSTANCE;
  }

}
