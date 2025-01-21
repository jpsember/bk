package bk.gen.rules;

import java.util.Arrays;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSMap;

public class Rule implements AbstractData {

  public String description() {
    return mDescription;
  }

  public int[] accounts() {
    return mAccounts;
  }

  public ActionName action() {
    return mAction;
  }

  public double percent() {
    return mPercent;
  }

  public int sourceAccount() {
    return mSourceAccount;
  }

  public int targetAccount() {
    return mTargetAccount;
  }

  public String dateMin() {
    return mDateMin;
  }

  public String dateMax() {
    return mDateMax;
  }

  public long parsedDateMin() {
    return mParsedDateMin;
  }

  public long parsedDateMax() {
    return mParsedDateMax;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "description";
  protected static final String _1 = "accounts";
  protected static final String _2 = "action";
  protected static final String _3 = "percent";
  protected static final String _4 = "source_account";
  protected static final String _5 = "target_account";
  protected static final String _6 = "date_min";
  protected static final String _7 = "date_max";
  protected static final String _8 = "parsed_date_min";
  protected static final String _9 = "parsed_date_max";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mDescription);
    m.putUnsafe(_1, DataUtil.encodeBase64Maybe(mAccounts));
    m.putUnsafe(_2, mAction.toString().toLowerCase());
    m.putUnsafe(_3, mPercent);
    m.putUnsafe(_4, mSourceAccount);
    m.putUnsafe(_5, mTargetAccount);
    m.putUnsafe(_6, mDateMin);
    m.putUnsafe(_7, mDateMax);
    m.putUnsafe(_8, mParsedDateMin);
    m.putUnsafe(_9, mParsedDateMax);
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
    mDescription = m.opt(_0, "");
    {
      mAccounts = DataUtil.EMPTY_INT_ARRAY;
      Object x = m.optUnsafe(_1);
      if (x != null) {
        mAccounts = DataUtil.parseIntsFromArrayOrBase64(x);
      }
    }
    {
      String x = m.opt(_2, "");
      mAction = x.isEmpty() ? ActionName.DEFAULT_INSTANCE : ActionName.valueOf(x.toUpperCase());
    }
    mPercent = m.opt(_3, 0.0);
    mSourceAccount = m.opt(_4, 0);
    mTargetAccount = m.opt(_5, 0);
    mDateMin = m.opt(_6, "");
    mDateMax = m.opt(_7, "");
    mParsedDateMin = m.opt(_8, 0L);
    mParsedDateMax = m.opt(_9, 0L);
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
    if (!(mDescription.equals(other.mDescription)))
      return false;
    if (!(Arrays.equals(mAccounts, other.mAccounts)))
      return false;
    if (!(mAction.equals(other.mAction)))
      return false;
    if (!(mPercent == other.mPercent))
      return false;
    if (!(mSourceAccount == other.mSourceAccount))
      return false;
    if (!(mTargetAccount == other.mTargetAccount))
      return false;
    if (!(mDateMin.equals(other.mDateMin)))
      return false;
    if (!(mDateMax.equals(other.mDateMax)))
      return false;
    if (!(mParsedDateMin == other.mParsedDateMin))
      return false;
    if (!(mParsedDateMax == other.mParsedDateMax))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mDescription.hashCode();
      r = r * 37 + Arrays.hashCode(mAccounts);
      r = r * 37 + mAction.ordinal();
      r = r * 37 + (int) mPercent;
      r = r * 37 + mSourceAccount;
      r = r * 37 + mTargetAccount;
      r = r * 37 + mDateMin.hashCode();
      r = r * 37 + mDateMax.hashCode();
      r = r * 37 + (int)mParsedDateMin;
      r = r * 37 + (int)mParsedDateMax;
      m__hashcode = r;
    }
    return r;
  }

  protected String mDescription;
  protected int[] mAccounts;
  protected ActionName mAction;
  protected double mPercent;
  protected int mSourceAccount;
  protected int mTargetAccount;
  protected String mDateMin;
  protected String mDateMax;
  protected long mParsedDateMin;
  protected long mParsedDateMax;
  protected int m__hashcode;

  public static final class Builder extends Rule {

    private Builder(Rule m) {
      mDescription = m.mDescription;
      mAccounts = m.mAccounts;
      mAction = m.mAction;
      mPercent = m.mPercent;
      mSourceAccount = m.mSourceAccount;
      mTargetAccount = m.mTargetAccount;
      mDateMin = m.mDateMin;
      mDateMax = m.mDateMax;
      mParsedDateMin = m.mParsedDateMin;
      mParsedDateMax = m.mParsedDateMax;
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
      r.mDescription = mDescription;
      r.mAccounts = mAccounts;
      r.mAction = mAction;
      r.mPercent = mPercent;
      r.mSourceAccount = mSourceAccount;
      r.mTargetAccount = mTargetAccount;
      r.mDateMin = mDateMin;
      r.mDateMax = mDateMax;
      r.mParsedDateMin = mParsedDateMin;
      r.mParsedDateMax = mParsedDateMax;
      return r;
    }

    public Builder description(String x) {
      mDescription = (x == null) ? "" : x;
      return this;
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

    public Builder sourceAccount(int x) {
      mSourceAccount = x;
      return this;
    }

    public Builder targetAccount(int x) {
      mTargetAccount = x;
      return this;
    }

    public Builder dateMin(String x) {
      mDateMin = (x == null) ? "" : x;
      return this;
    }

    public Builder dateMax(String x) {
      mDateMax = (x == null) ? "" : x;
      return this;
    }

    public Builder parsedDateMin(long x) {
      mParsedDateMin = x;
      return this;
    }

    public Builder parsedDateMax(long x) {
      mParsedDateMax = x;
      return this;
    }

  }

  public static final Rule DEFAULT_INSTANCE = new Rule();

  private Rule() {
    mDescription = "";
    mAccounts = DataUtil.EMPTY_INT_ARRAY;
    mAction = ActionName.DEFAULT_INSTANCE;
    mDateMin = "";
    mDateMax = "";
  }

}
