package bk;

import static bk.Util.devMode;
import static js.base.Tools.*;

import js.base.BaseObject;
import js.data.DataUtil;
import js.json.JSMap;

public class Trie extends BaseObject {

  public static void db(Object... messages) {
    if (false && devMode())
      pr(insertStringToFront("<TRIE:>", messages));
  }

  public Trie addSentence(String inputSentence, String optOutputSentence) {
    db("addSentence:", inputSentence);
    // It should perhaps automatically add words for all sentences, AFTER the sentences have been added? Or do we get the same tri?
    optOutputSentence = ifNullOrEmpty(optOutputSentence, inputSentence);
    add(inputSentence, optOutputSentence, true);
    for (var wd : split(inputSentence, ' '))
      addWord(wd, optOutputSentence);
    return this;
  }

  public void addWord(String inputWord, String outputSentence) {
    add(inputWord, outputSentence, false);
  }

  private void add(String inputText, String outputSentence, boolean sentenceFlag) {
    if (verbose())
      log("add", (sentenceFlag ? "sentence:" : "word:"), quote(inputText), "outputText:",
          quote(outputSentence));
    if (inputText.isEmpty())
      return;
    var textBytes = toLowerCaseLetters(inputText);
    if (textBytes == null) {
      log("non-ASCII character found in text:", inputText);
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
        nextNode.isSentencePrefix = sentenceFlag;
        nextNode.answer = outputSentence;
        node.addChild(nextLetter, nextNode);
      } else {
        nextNode = node.childNodes[ci];
      }
      node = nextNode;

      // Update answer for current node if current prefix is "better"
      boolean update = node.answer == null;
      while (!update) {
        if (sentenceFlag) {
          if (node.isSentencePrefix && node.answer.length() <= inputText.length())
            break;
          update = true;
        } else {
          if (node.isSentencePrefix || node.answer.length() <= inputText.length())
            break;
          update = true;
        }
      }
      if (update) {
        // if (devMode()) pr("...update node sentenceFlag:",sentenceFlag,quote(outputSentence));
        node.isSentencePrefix = sentenceFlag;
        node.answer = outputSentence;
      }
    }
  }

  public String query(String text) {
    String result = "";

    if (!text.isEmpty()) {
      var textBytes = toLowerCaseLetters(text);
      var node = mRoot;

      Node best = null;
      for (int i = 0; i < textBytes.length; i++) {
        byte c = textBytes[i];
        var ci = indexOf(node.childLetters, c);
        if (ci < 0)
          return "";
        var ch = node.childNodes[ci];
        node = ch;
        best = node;
      }
      result = best.answer;
    }
    if (devMode()) {
      db("query, text:", quote(text), "==>", quote(result));
      db("trie:", INDENT, toJson());
    }
    return result;
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

    @Override
    public String toString() {
      var m = map();
      m.put("", "Node");
      m.put("# children", used);
      m.put("answer", (isSentencePrefix ? "S:" : "W:") + answer);
      return m.prettyPrint();
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

  @Override
  public JSMap toJson() {
    var m = map();
    auxMap(m, mRoot);
    return m;
  }

  private void auxMap(JSMap m, Node n) {
    if (n != mRoot) {
      var label = (n.isSentencePrefix ? "S:" : "W:") + n.answer;
      m.put("", label);
    }
    for (int i = 0; i < n.used; i++) {
      var c = n.childLetters[i];
      var cn = n.childNodes[i];
      var m2 = map();
      m.put(Character.toString((char) c), m2);
      auxMap(m2, cn);
    }
  }

  private Node mRoot = new Node();

}
