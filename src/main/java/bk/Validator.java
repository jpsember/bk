package bk;

public interface Validator {

  default String validate(String value) {
    return value;
  }

}
