package bk.gen;

import java.io.File;
import js.data.AbstractData;
import js.file.Files;
import js.json.JSMap;

public class BkConfig implements AbstractData {

  public boolean testing() {
    return mTesting;
  }

  public boolean create() {
    return mCreate;
  }

  public File database() {
    return mDatabase;
  }

  public File logFile() {
    return mLogFile;
  }

  public boolean printPdf() {
    return mPrintPdf;
  }

  public boolean applyHintToTransaction() {
    return mApplyHintToTransaction;
  }

  public boolean generateTestData() {
    return mGenerateTestData;
  }

  public String closeAccounts() {
    return mCloseAccounts;
  }

  public boolean devMode() {
    return mDevMode;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "testing";
  protected static final String _1 = "create";
  protected static final String _2 = "database";
  protected static final String _3 = "log_file";
  protected static final String _4 = "print_pdf";
  protected static final String _5 = "apply_hint_to_transaction";
  protected static final String _6 = "generate_test_data";
  protected static final String _7 = "close_accounts";
  protected static final String _8 = "dev_mode";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mTesting);
    m.putUnsafe(_1, mCreate);
    m.putUnsafe(_2, mDatabase.toString());
    m.putUnsafe(_3, mLogFile.toString());
    m.putUnsafe(_4, mPrintPdf);
    m.putUnsafe(_5, mApplyHintToTransaction);
    m.putUnsafe(_6, mGenerateTestData);
    m.putUnsafe(_7, mCloseAccounts);
    m.putUnsafe(_8, mDevMode);
    return m;
  }

  @Override
  public BkConfig build() {
    return this;
  }

  @Override
  public BkConfig parse(Object obj) {
    return new BkConfig((JSMap) obj);
  }

  private BkConfig(JSMap m) {
    mTesting = m.opt(_0, false);
    mCreate = m.opt(_1, false);
    {
      mDatabase = _D2;
      String x = m.opt(_2, (String) null);
      if (x != null) {
        mDatabase = new File(x);
      }
    }
    {
      mLogFile = Files.DEFAULT;
      String x = m.opt(_3, (String) null);
      if (x != null) {
        mLogFile = new File(x);
      }
    }
    mPrintPdf = m.opt(_4, false);
    mApplyHintToTransaction = m.opt(_5, false);
    mGenerateTestData = m.opt(_6, false);
    mCloseAccounts = m.opt(_7, "");
    mDevMode = m.opt(_8, false);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof BkConfig))
      return false;
    BkConfig other = (BkConfig) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mTesting == other.mTesting))
      return false;
    if (!(mCreate == other.mCreate))
      return false;
    if (!(mDatabase.equals(other.mDatabase)))
      return false;
    if (!(mLogFile.equals(other.mLogFile)))
      return false;
    if (!(mPrintPdf == other.mPrintPdf))
      return false;
    if (!(mApplyHintToTransaction == other.mApplyHintToTransaction))
      return false;
    if (!(mGenerateTestData == other.mGenerateTestData))
      return false;
    if (!(mCloseAccounts.equals(other.mCloseAccounts)))
      return false;
    if (!(mDevMode == other.mDevMode))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (mTesting ? 1 : 0);
      r = r * 37 + (mCreate ? 1 : 0);
      r = r * 37 + mDatabase.hashCode();
      r = r * 37 + mLogFile.hashCode();
      r = r * 37 + (mPrintPdf ? 1 : 0);
      r = r * 37 + (mApplyHintToTransaction ? 1 : 0);
      r = r * 37 + (mGenerateTestData ? 1 : 0);
      r = r * 37 + mCloseAccounts.hashCode();
      r = r * 37 + (mDevMode ? 1 : 0);
      m__hashcode = r;
    }
    return r;
  }

  protected boolean mTesting;
  protected boolean mCreate;
  protected File mDatabase;
  protected File mLogFile;
  protected boolean mPrintPdf;
  protected boolean mApplyHintToTransaction;
  protected boolean mGenerateTestData;
  protected String mCloseAccounts;
  protected boolean mDevMode;
  protected int m__hashcode;

  public static final class Builder extends BkConfig {

    private Builder(BkConfig m) {
      mTesting = m.mTesting;
      mCreate = m.mCreate;
      mDatabase = m.mDatabase;
      mLogFile = m.mLogFile;
      mPrintPdf = m.mPrintPdf;
      mApplyHintToTransaction = m.mApplyHintToTransaction;
      mGenerateTestData = m.mGenerateTestData;
      mCloseAccounts = m.mCloseAccounts;
      mDevMode = m.mDevMode;
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
    public BkConfig build() {
      BkConfig r = new BkConfig();
      r.mTesting = mTesting;
      r.mCreate = mCreate;
      r.mDatabase = mDatabase;
      r.mLogFile = mLogFile;
      r.mPrintPdf = mPrintPdf;
      r.mApplyHintToTransaction = mApplyHintToTransaction;
      r.mGenerateTestData = mGenerateTestData;
      r.mCloseAccounts = mCloseAccounts;
      r.mDevMode = mDevMode;
      return r;
    }

    public Builder testing(boolean x) {
      mTesting = x;
      return this;
    }

    public Builder create(boolean x) {
      mCreate = x;
      return this;
    }

    public Builder database(File x) {
      mDatabase = (x == null) ? _D2 : x;
      return this;
    }

    public Builder logFile(File x) {
      mLogFile = (x == null) ? Files.DEFAULT : x;
      return this;
    }

    public Builder printPdf(boolean x) {
      mPrintPdf = x;
      return this;
    }

    public Builder applyHintToTransaction(boolean x) {
      mApplyHintToTransaction = x;
      return this;
    }

    public Builder generateTestData(boolean x) {
      mGenerateTestData = x;
      return this;
    }

    public Builder closeAccounts(String x) {
      mCloseAccounts = (x == null) ? "" : x;
      return this;
    }

    public Builder devMode(boolean x) {
      mDevMode = x;
      return this;
    }

  }

  private static final File _D2 = new File("database.json");

  public static final BkConfig DEFAULT_INSTANCE = new BkConfig();

  private BkConfig() {
    mDatabase = _D2;
    mLogFile = Files.DEFAULT;
    mCloseAccounts = "";
  }

}
