package bk;

import static js.base.Tools.*;
import static org.junit.Assert.*;

import org.junit.Test;

import js.json.JSMap;
import js.testutil.MyTestCase;

public class TrieTest extends MyTestCase {

  @Test
  public void small() {
    var s = "a";
    addWords(s, "w");
    tri().addSentence(s, "s");
    log("Tri:", INDENT, tri());
    ask("a");
  }

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

  @Test
  public void sentenceOverWords() {
    var s = "alpha beta gamma";
    addWords(s, "w");
    tri().addSentence(s, "s");
    ask("alpha", "beta", "gamma");
    log("Tri:", INDENT, tri());
  }

  @Test
  public void shortWordsOverLonger() {
    addWords("alpha", "alpha");
    addWords("alph", "alph");
    addWords("al", "al");

    assertEquals("al", tri().query("al"));
  }

  @Test
  public void b() {
    tri().addSentence("1200 Foo", "s1");
    tri().addSentence("5050 Management and admin fees", "s2");
    log("Tri:", INDENT, tri());
    ask("f", "fe", "fee", "feeb");
  }

  private void add(String... strs) {
    // Add a prefix to demonstrate that the output sentence can differ from the input one
    for (var x : strs)
      tri().addSentence(x, "s");
    for (var x : strs)
      tri().addWord(x, "w");
  }

  private void ask(String... strs) {
    for (var s : strs) {
      var res = tri().query(s);
      result().putNumbered(s, res);
    }
    generateMessage(result().prettyPrint());
    assertGenerated();
  }

  private Trie tri() {
    if (mTri == null) {
      mTri = new Trie();
      if (verbose())
        mTri.setVerbose();
    }
    return mTri;
  }

  private void addWords(String spaceDelimitedWords, String result) {
    for (var x : split(spaceDelimitedWords, ' '))
      tri().addWord(x, result);
  }

  private JSMap result() {
    if (mRes == null)
      mRes = map();
    return mRes;
  }

  private Trie mTri;
  private JSMap mRes;
}
