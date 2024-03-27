package com.pbear.lib.common;

public class FieldNotValidException extends RuntimeException {
  public <T> FieldNotValidException(final Class<T> clz) {
    super("not valid field, class: " + clz.getCanonicalName());
  }
}
