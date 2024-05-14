package com.pbear.starter.webflux.util;

import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.stream.Stream;

public class RestUtil {
  private static final String REQ_PARAM_PAGE = "page";
  private static final String REQ_PARAM_SIZE = "size";
  private static final String PAGE_DEFAULT = "0";
  private static final String SIZE_DEFAULT = "10";

  public static PageParam parsePageParameter(final ServerRequest serverRequest) {
    return new PageParam(
        Long.parseLong(serverRequest.queryParam(REQ_PARAM_PAGE).orElse(PAGE_DEFAULT)),
        Long.parseLong(serverRequest.queryParam(REQ_PARAM_SIZE).orElse(SIZE_DEFAULT)));
  }

  public static <T> Stream<T> applyPage(final Stream<T> source, final PageParam pageParam) {
    return source
        .skip(pageParam.page() * pageParam.size())
        .limit(pageParam.size());
  }
}
