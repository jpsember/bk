package bk;

public interface TextEditHandler extends WindowHandler, FocusHandler {

  default String validate(String text) {
    return text;
  }

}
