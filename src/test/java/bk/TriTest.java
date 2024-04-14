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

  @Test
  public void longerSentencePriorityOverShorterWord() {
    add("abc def", "ha ab");
    ask("a", "ab", "abc", "abcd", "b");
  }

  @Test
  public void wordMatch() {
    add("abc def");
    ask("de", "def", "defg");
  }

  private void add(String... strs) {
    for (var x : strs)
      tri().addSentence(x);
    for (var x : strs)
      tri().addWords(x);
  }

  private void ask(String... strs) {
    for (var s : strs) {
      var res = tri().query(s);
      result().putNumbered(s, res);
    }
    generateMessage(result().prettyPrint());
    assertGenerated();
  }

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

  private Tri mTri;
  private JSMap mRes;
}
