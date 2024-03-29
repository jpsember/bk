package bk;

public interface TextEditHandler extends WindowHandler {

  default String validate(String text) {
    return text;
  }

}
