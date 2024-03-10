package com.pbear.starter.webflux.data.exception;

import org.springframework.http.HttpStatus;

public class PassportNotExistException extends RestException {
  public PassportNotExistException() {
    super(HttpStatus.BAD_REQUEST, "E4000", "passport not exist");
  }
}
