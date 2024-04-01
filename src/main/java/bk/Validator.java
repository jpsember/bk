package bk;

public interface Validator {

  default ValidationResult validate(String value) {
    return ValidationResult.NONE;
  }

}
