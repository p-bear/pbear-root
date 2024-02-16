package com.pbear.starter.webflux.data.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.server.ResponseStatusException;

@Getter
public class RestException extends ResponseStatusException {
  private final String code;
  private final Object details;

  public RestException(final HttpStatusCode status, final String code) {
    super(status);
    this.code = code;
    this.details = null;
  }

  public RestException(final String code, final Object details) {
    super(HttpStatus.INTERNAL_SERVER_ERROR);
    this.code = code;
    this.details = details;
  }

  public RestException(final HttpStatusCode status, final String code, final Object details) {
    super(status);
    this.code = code;
    this.details = details;
  }
}
