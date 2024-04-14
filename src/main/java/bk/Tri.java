package bk;

import static js.base.Tools.*;

import java.util.List;

import js.base.BaseObject;
import js.data.ShortArray;

public class Tri extends BaseObject {

  public Tri addSentence(String st) {
    todo("update core to support indexOf in shortArray");
    add(st, true);return this;
  }

  public Tri addWords(String st) {
    for (var wd : split(st, ' ')) {
      add(wd, false);
    }return this;
  }

  private void add(String text, boolean sentence) {
    log("add", sentence ? "sentence:" : "word:", text);
    if (text.isEmpty())
      return;

    var textLowerCase = text.toLowerCase();
    var node = mRoot;

    for (int i = 0; i < textLowerCase.length(); i++) {
      // Determine next node
      Node nextNode;
      var charAsShort = (short) textLowerCase.charAt(i);
      char c = textLowerCase.charAt(i);
      var ci = indexOf(node.childChars, charAsShort);
      if (ci < 0) {
        nextNode = new Node();
        nextNode.answer = text;
        log("...creating new node for", i, "char:", c);
        node.childChars.add(charAsShort);
        node.childNodes.add(nextNode);
      } else
        nextNode = node.childNodes.get(ci);

      if (i > 0) {
        // Update answer for current node if current prefix is better
        boolean update = node.answer == null;
        while (!update) {
          if (sentence) {
            if (node.isSentencePrefix && node.answer.length() <= text.length())
              break;
            update = true;
          } else {
            if (node.isSentencePrefix || node.answer.length() <= text.length())
              break;
            update = true;
          }

        }
        if (update) {
          node.isSentencePrefix = sentence;
          node.answer = text;
        }
      }
      node = nextNode;
    }
  }

  private static int indexOf(ShortArray shortArray, short x) {
    int size = shortArray.size();
    for (int i = 0; i < size; i++)
      if (shortArray.get(i) == x)
        return i;
    return -1;
  }

  public String query(String text) {
    if (text.isEmpty())
      return "";
    var textLowerCase = text.toLowerCase();
    var node = mRoot;

    for (int i = 0; i < textLowerCase.length(); i++) {
      char c = textLowerCase.charAt(i);
      var charAsShort = (short) c;
      var ci = indexOf(node.childChars, charAsShort);
      if (ci < 0)
        return "";
      node = node.childNodes.get(ci);
    }
    return node.answer;
  }

  private static class Node {
    String answer; // best match for prefix up to this point
    boolean isSentencePrefix = true; // true if the answer is a prefix of the path
    ShortArray.Builder childChars = ShortArray.newBuilder();
    List<Node> childNodes = arrayList();
  }

  private Node mRoot = new Node();
}
