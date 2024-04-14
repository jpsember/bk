package bk;

import js.base.BaseObject;
import static js.base.Tools.*;

public class Tri extends BaseObject {

  public void addSentence(String st) {
    add(st, true);
  }

  public void addWord(String st) {
    add(st, false);
  }

  private void add(String phrase, boolean sentence) {
    todo("finish this");
    if (phrase.isEmpty())
      return;
  }

  public String query(String s) {
    todo("and this");
    return "";
  }

  {
    loadTools();
  }
}
