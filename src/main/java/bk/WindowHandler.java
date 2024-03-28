package bk;

import com.googlecode.lanterna.input.KeyStroke;

public interface WindowHandler {

  default void paint() {
  }
//
//  default void paintPartial() {
//  }

  default void processKeyStroke(JWindow window, KeyStroke k) {
  }
}
