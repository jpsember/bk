package bk;

import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import static js.base.Tools.*;

/**
 * Wraps a KeyStroke to a string that includes information about Alt, Shift,
 * Control keys.
 * 
 * "[A][C][S]:{character or KeyType name}"
 */
public class KeyEvent {

  public static final String ENTER = ":Enter";
  public static final String ESCAPE = ":Escape";

  public KeyEvent(KeyStroke k) {
    mKeyStroke = k;
    var sb = new StringBuilder();
    if (k.isAltDown())
      sb.append('A');
    if (k.isCtrlDown())
      sb.append('C');
    if (k.isShiftDown())
      sb.append('S');
    sb.append(':');
    switch (k.getKeyType()) {
    case Character:
      sb.append(k.getCharacter());
      break;
    default:
      sb.append(k.getKeyType().name());
      break;
    }
    mString = sb.toString();
    //pr("built KeyEvent from keystroke:", k, "string:", mString);
  }

  @Override
  public String toString() {
    return mString;
  }

  public boolean is(String str) {
    return mString.equals(str);
  }

  private String mString;
  private KeyStroke mKeyStroke;

  public KeyType keyType() {
    return mKeyStroke.getKeyType();
  }

  public Character getCharacter() {
    checkArgument(keyType() == KeyType.Character);
    return mKeyStroke.getCharacter();
  }

}
