package com.pbear.user.core.data.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class UserNotFoundException extends RuntimeException {
  private final String query;
}
