package bk;

import js.base.BaseObject;
import static js.base.Tools.*;

import static bk.Util.*;

public class UndoManager extends BaseObject {

  public static final UndoManager SHARED_INSTANCE = new UndoManager();

  private UndoManager() {
    loadTools();
    loadUtil();
    alertVerbose();
  }
}
