package bk;

import static js.base.Tools.*;

import org.junit.Test;

import js.json.JSMap;
import js.testutil.MyTestCase;
import static bk.Util.*;

public class ShareTest extends MyTestCase {

  @Test
  public void parse() {
    p("");
    p("alpha");
    p("12345");
    p("+123.456 bought");
    p("-123.456 sold");
    p("=0.234 initial");
    p("=.2345 assign");
    p("+1wtf23");
    p("=1.1.2wtf");
    assertRes();
  }

  private void p(String exp) {
    var s = parseShareInfo(exp);
    mRes.put(exp, s.toJson());
  }

  private void assertRes() {
    generateMessage(mRes.prettyPrint());
    assertGenerated();
  }

  private JSMap mRes = map();
}
