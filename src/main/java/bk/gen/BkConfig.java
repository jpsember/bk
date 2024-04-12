package bk.gen;

import java.io.File;
import js.data.AbstractData;
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

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "create";
  protected static final String _1 = "database";
  protected static final String _2 = "log_file";

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
      mLogFile = _D2;
      String x = m.opt(_2, (String) null);
      if (x != null) {
        mLogFile = new File(x);
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
      m__hashcode = r;
    }
    return r;
  }

  protected boolean mCreate;
  protected File mDatabase;
  protected File mLogFile;
  protected int m__hashcode;

  public static final class Builder extends BkConfig {

    private Builder(BkConfig m) {
      mCreate = m.mCreate;
      mDatabase = m.mDatabase;
      mLogFile = m.mLogFile;
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
      mLogFile = (x == null) ? _D2 : x;
      return this;
    }

  }

  private static final File _D1 = new File("database.json");
  private static final File _D2 = new File("bk_log.txt");

  public static final BkConfig DEFAULT_INSTANCE = new BkConfig();

  private BkConfig() {
    mDatabase = _D1;
    mLogFile = _D2;
  }

}
