package bk;

public interface FocusHandler {

  default void gainFocus() {
  }

  default void loseFocus() {
  }

  default void processKeyEvent(KeyEvent k) {
  }

  default void repaint() {
  }

}