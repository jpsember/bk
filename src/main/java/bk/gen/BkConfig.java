package bk.gen;

import java.io.File;
import js.data.AbstractData;
import js.file.Files;
import js.json.JSMap;

public class BkConfig implements AbstractData {

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

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "create";
  protected static final String _1 = "database";
  protected static final String _2 = "log_file";
  protected static final String _3 = "print_pdf";
  protected static final String _4 = "apply_hint_to_transaction";
  protected static final String _5 = "generate_test_data";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mCreate);
    m.putUnsafe(_1, mDatabase.toString());
    m.putUnsafe(_2, mLogFile.toString());
    m.putUnsafe(_3, mPrintPdf);
    m.putUnsafe(_4, mApplyHintToTransaction);
    m.putUnsafe(_5, mGenerateTestData);
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
    mCreate = m.opt(_0, false);
    {
      mDatabase = _D1;
      String x = m.opt(_1, (String) null);
      if (x != null) {
        mDatabase = new File(x);
      }
    }
    {
      mLogFile = Files.DEFAULT;
      String x = m.opt(_2, (String) null);
      if (x != null) {
        mLogFile = new File(x);
      }
    }
    mPrintPdf = m.opt(_3, false);
    mApplyHintToTransaction = m.opt(_4, false);
    mGenerateTestData = m.opt(_5, false);
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
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (mCreate ? 1 : 0);
      r = r * 37 + mDatabase.hashCode();
      r = r * 37 + mLogFile.hashCode();
      r = r * 37 + (mPrintPdf ? 1 : 0);
      r = r * 37 + (mApplyHintToTransaction ? 1 : 0);
      r = r * 37 + (mGenerateTestData ? 1 : 0);
      m__hashcode = r;
    }
    return r;
  }

  protected boolean mCreate;
  protected File mDatabase;
  protected File mLogFile;
  protected boolean mPrintPdf;
  protected boolean mApplyHintToTransaction;
  protected boolean mGenerateTestData;
  protected int m__hashcode;

  public static final class Builder extends BkConfig {

    private Builder(BkConfig m) {
      mCreate = m.mCreate;
      mDatabase = m.mDatabase;
      mLogFile = m.mLogFile;
      mPrintPdf = m.mPrintPdf;
      mApplyHintToTransaction = m.mApplyHintToTransaction;
      mGenerateTestData = m.mGenerateTestData;
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
      r.mCreate = mCreate;
      r.mDatabase = mDatabase;
      r.mLogFile = mLogFile;
      r.mPrintPdf = mPrintPdf;
      r.mApplyHintToTransaction = mApplyHintToTransaction;
      r.mGenerateTestData = mGenerateTestData;
      return r;
    }

    public Builder create(boolean x) {
      mCreate = x;
      return this;
    }

    public Builder database(File x) {
      mDatabase = (x == null) ? _D1 : x;
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

  }

  private static final File _D1 = new File("database.json");

  public static final BkConfig DEFAULT_INSTANCE = new BkConfig();

  private BkConfig() {
    mDatabase = _D1;
    mLogFile = Files.DEFAULT;
  }

}
