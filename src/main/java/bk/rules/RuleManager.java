package bk.rules;

import js.base.BaseObject;
import static js.base.Tools.*;

public class RuleManager extends BaseObject {

  public static final RuleManager SHARED_INSTANCE = new RuleManager();
  static {
    loadTools();
  }
}
