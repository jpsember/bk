package bk.gen;

import java.io.File;
import js.data.AbstractData;
import js.file.Files;
import js.json.JSMap;

public class BkConfig implements AbstractData {

  public boolean create() {
    return mCreate;
  }

  public File file() {
    return mFile;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "create";
  protected static final String _1 = "file";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mCreate);
    m.putUnsafe(_1, mFile.toString());
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
      mFile = Files.DEFAULT;
      String x = m.opt(_1, (String) null);
      if (x != null) {
        mFile = new File(x);
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
    if (!(mFile.equals(other.mFile)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + (mCreate ? 1 : 0);
      r = r * 37 + mFile.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected boolean mCreate;
  protected File mFile;
  protected int m__hashcode;

  public static final class Builder extends BkConfig {

    private Builder(BkConfig m) {
      mCreate = m.mCreate;
      mFile = m.mFile;
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
      r.mFile = mFile;
      return r;
    }

    public Builder create(boolean x) {
      mCreate = x;
      return this;
    }

    public Builder file(File x) {
      mFile = (x == null) ? Files.DEFAULT : x;
      return this;
    }

  }

  public static final BkConfig DEFAULT_INSTANCE = new BkConfig();

  private BkConfig() {
    mFile = Files.DEFAULT;
  }

}
