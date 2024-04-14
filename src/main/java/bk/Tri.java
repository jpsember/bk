package bk;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.data.DataUtil;

public class Tri extends BaseObject {

  public Tri addSentence(String st) {
    add(st, true);
    return this;
  }

  public Tri addWords(String st) {
    for (var wd : split(st, ' ')) {
      add(wd, false);
    }
    return this;
  }

  private void add(String text, boolean sentence) {
    log("add", sentence ? "sentence:" : "word:", text);
    if (text.isEmpty())
      return;
    var textBytes = toLowerCaseLetters(text);
    if (textBytes == null) {
      log("non-ASCII character found in text:", text);
      return;
    }

    var node = mRoot;

    for (int i = 0; i < textBytes.length; i++) {
      // Determine next node
      Node nextNode;
      byte nextLetter = textBytes[i];
      var ci = indexOf(node.childLetters, textBytes[i]);
      if (ci < 0) {
        nextNode = new Node();
        nextNode.answer = text;
        log("...creating new node for", i, "char:", nextLetter);
        node.addChild(nextLetter, nextNode);
      } else
        nextNode = node.childNodes[ci];

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

  public String query(String text) {
    if (text.isEmpty())
      return "";
    var textBytes = toLowerCaseLetters(text);
    var node = mRoot;

    for (int i = 0; i < textBytes.length; i++) {
      byte c = textBytes[i];
      var ci = indexOf(node.childLetters, c);
      if (ci < 0)
        return "";
      node = node.childNodes[ci];
    }
    return node.answer;
  }

  private static final Node[] EMPTY_NODE_ARRAY = new Node[0];

  private static class Node {
    String answer; // best match for prefix up to this point
    boolean isSentencePrefix = true; // true if the answer is a prefix of the path
    byte[] childLetters = DataUtil.EMPTY_BYTE_ARRAY;
    Node[] childNodes = EMPTY_NODE_ARRAY;
    int used;

    void addChild(byte ch, Node child) {
      if (childLetters.length == used) {
        int s = Math.max(5, used * 2);
        var newLetters = new byte[s];
        var newNodes = new Node[s];
        if (used > 0) {
          System.arraycopy(childLetters, 0, newLetters, 0, used);
          System.arraycopy(childNodes, 0, newNodes, 0, used);
        }
        childLetters = newLetters;
        childNodes = newNodes;
      }
      childLetters[used] = ch;
      childNodes[used] = child;
      used++;
    }
  }

  private static byte[] toLowerCaseLetters(String s) {
    var x = s.length();
    var b = new byte[x];
    for (int i = 0; i < x; i++) {
      int c = s.charAt(i);
      if (c > 127)
        return null;
      if (c >= 'a' && c <= 'z')
        c += ('A' - 'a');
      b[i] = (byte) c;
    }
    return b;
  }

  private static int indexOf(byte[] shortArray, byte x) {
    int s = shortArray.length;
    while (--s >= 0)
      if (shortArray[s] == x)
        break;
    return s;
  }

  private Node mRoot = new Node();
}
