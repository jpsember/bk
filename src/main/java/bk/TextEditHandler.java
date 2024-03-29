package bk;

import com.googlecode.lanterna.input.KeyStroke;

public interface TextEditHandler extends WindowHandler, FocusHandler {

  default String validate(String text) {
    return text;
  }

}
