package bk;

import com.googlecode.lanterna.input.KeyStroke;

public interface FocusHandler {

  default void gainFocus() {
  }

  default void loseFocus() {
  }

  default void processKeyStroke(KeyStroke k) {
  }
}
