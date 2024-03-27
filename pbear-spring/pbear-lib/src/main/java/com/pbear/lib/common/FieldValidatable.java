package com.pbear.lib.common;

@FunctionalInterface
public interface FieldValidatable {
  boolean isValid();
  default boolean hasNullField(final Object... args) {
    for (Object arg : args) {
      if (arg == null) {
        return true;
      }
    }
    return false;
  }
}
