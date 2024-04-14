package bk;

import static js.base.Tools.*;

import org.junit.Test;

import js.json.JSMap;
import js.testutil.MyTestCase;

public class TriTest extends MyTestCase {

  @Test
  public void a() {
    add("abc");
    ask("a", "ab", "abc", "abcd", "b");
  }

  private void add(Object... strs) {
    for (var x : strs) {
      tri().add(x.toString());
    }
  }

  private void ask(String... strs) {
    for (var s : strs) {
      var res = tri().query(s);
      result().putNumbered(s, res);
    }
    var x = result().prettyPrint();
    log(x);
    assertHash(x);
  }

  private Tri mTri;

  private Tri tri() {
    if (mTri == null) {
      mTri = new Tri();
      if (verbose())
        mTri.setVerbose();
    }
    return mTri;
  }

  private JSMap result() {
    if (mRes == null)
      mRes = map();
    return mRes;
  }

  private JSMap mRes;
}
