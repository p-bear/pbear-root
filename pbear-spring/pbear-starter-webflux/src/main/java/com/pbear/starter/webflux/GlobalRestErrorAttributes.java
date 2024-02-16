package com.pbear.starter.webflux;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.starter.webflux.data.dto.CommonRestResponse;
import com.pbear.starter.webflux.data.exception.RestException;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Map;

@Component
public class GlobalRestErrorAttributes extends DefaultErrorAttributes {
  private static final ObjectMapper OBJECT_MAPPER;

  static {
    OBJECT_MAPPER = new ObjectMapper();
  }

  @Override
  public Map<String, Object> getErrorAttributes(final ServerRequest request, final ErrorAttributeOptions options) {
    Throwable throwable = getError(request);
    CommonRestResponse.CommonRestResponseBuilder responseBuilder = CommonRestResponse.builder()
        .result("fail")
        .code("E500")
        .message(throwable.getMessage());
    RestException restException = this.getInnerRestException(throwable);
    if (restException != null) {
      responseBuilder
          .code(restException.getCode())
          .details(restException.getDetails());
    }

    return OBJECT_MAPPER.convertValue(responseBuilder.build(), new TypeReference<>() {});
  }

  private RestException getInnerRestException(final Throwable throwable) {
    if (throwable instanceof RestException restException) {
      return restException;
    }
    if (throwable.getCause() != null) {
      return getInnerRestException(throwable.getCause());
    }
    return null;
  }
}
