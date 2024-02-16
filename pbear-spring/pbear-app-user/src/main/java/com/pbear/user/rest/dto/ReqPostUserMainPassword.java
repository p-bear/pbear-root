package com.pbear.user.rest.dto;

import com.pbear.starter.webflux.data.exception.RestException;
import org.springframework.http.HttpStatus;

public record ReqPostUserMainPassword(
    String mainId,
    String password
) {
  public ReqPostUserMainPassword {
    if (mainId == null || mainId.isEmpty()) {
      throw new RestException(HttpStatus.BAD_REQUEST, "E4000", "mainId=" + mainId);
    }
    if (password == null || password.isEmpty()) {
      throw new RestException(HttpStatus.BAD_REQUEST, "E4001", "password=" + password);
    }
  }
}
