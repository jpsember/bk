package bk.gen;

import java.util.List;
import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;

public class UndoAction implements AbstractData {

  public String description() {
    return mDescription;
  }

  public List<UndoEntry> entries() {
    return mEntries;
  }

  @Override
  public Builder toBuilder() {
    return new Builder(this);
  }

  protected static final String _0 = "description";
  protected static final String _1 = "entries";

  @Override
  public String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public JSMap toJson() {
    JSMap m = new JSMap();
    m.putUnsafe(_0, mDescription);
    {
      JSList j = new JSList();
      for (UndoEntry x : mEntries)
        j.add(x.toJson());
      m.put(_1, j);
    }
    return m;
  }

  @Override
  public UndoAction build() {
    return this;
  }

  @Override
  public UndoAction parse(Object obj) {
    return new UndoAction((JSMap) obj);
  }

  private UndoAction(JSMap m) {
    mDescription = m.opt(_0, "");
    mEntries = DataUtil.parseListOfObjects(UndoEntry.DEFAULT_INSTANCE, m.optJSList(_1), false);
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof UndoAction))
      return false;
    UndoAction other = (UndoAction) object;
    if (other.hashCode() != hashCode())
      return false;
    if (!(mDescription.equals(other.mDescription)))
      return false;
    if (!(mEntries.equals(other.mEntries)))
      return false;
    return true;
  }

  @Override
  public int hashCode() {
    int r = m__hashcode;
    if (r == 0) {
      r = 1;
      r = r * 37 + mDescription.hashCode();
      for (UndoEntry x : mEntries)
        if (x != null)
          r = r * 37 + x.hashCode();
      m__hashcode = r;
    }
    return r;
  }

  protected String mDescription;
  protected List<UndoEntry> mEntries;
  protected int m__hashcode;

  public static final class Builder extends UndoAction {

    private Builder(UndoAction m) {
      mDescription = m.mDescription;
      mEntries = DataUtil.mutableCopyOf(m.mEntries);
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
    public UndoAction build() {
      UndoAction r = new UndoAction();
      r.mDescription = mDescription;
      r.mEntries = DataUtil.immutableCopyOf(mEntries);
      return r;
    }

    public Builder description(String x) {
      mDescription = (x == null) ? "" : x;
      return this;
    }

    public Builder entries(List<UndoEntry> x) {
      mEntries = DataUtil.mutableCopyOf((x == null) ? DataUtil.emptyList() : x);
      return this;
    }

  }

  public static final UndoAction DEFAULT_INSTANCE = new UndoAction();

  private UndoAction() {
    mDescription = "";
    mEntries = DataUtil.emptyList();
  }

}
