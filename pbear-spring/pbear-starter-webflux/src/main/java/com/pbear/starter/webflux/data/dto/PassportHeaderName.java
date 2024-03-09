package com.pbear.starter.webflux.data.dto;

import lombok.Getter;

@Getter
public enum PassportHeaderName {
  ID("X-PP-id"),
  MAIN_ID("X-PP-mainId");

  private final String headerName;

  PassportHeaderName(final String headerName) {
    this.headerName = headerName;
  }
}
