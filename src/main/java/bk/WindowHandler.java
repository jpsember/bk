package bk;

import com.googlecode.lanterna.input.KeyStroke;

public interface WindowHandler {

  void paint(JWindow window);

  default void processKeyStroke(JWindow window, KeyStroke k) {
  }
}
