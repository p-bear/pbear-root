package com.pbear.starter.webflux.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import org.slf4j.MDC;

@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommonRestResponse(
    String result,
    String traceId,
    String code,
    String message,
    Object details,
    Object data) {
  @SuppressWarnings("FieldMayBeFinal")
  public static class CommonRestResponseBuilder {
    private String result = "success";
    private String traceId = MDC.get("traceId");
  }
}
