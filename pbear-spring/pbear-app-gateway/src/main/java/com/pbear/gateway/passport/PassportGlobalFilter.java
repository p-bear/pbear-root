package com.pbear.gateway.passport;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class PassportGlobalFilter implements GlobalFilter {
  private static final String PASSPORT_HEADER_PREFIX = "X-PP-";
  private final PassportService passportService;

  @Override
  public Mono<Void> filter(final ServerWebExchange exchange, final GatewayFilterChain chain) {
    return Mono
        .zip(this.handleExchangeWithPassport(exchange), Mono.just(chain))
        .flatMap(tuple -> tuple.getT2().filter(tuple.getT1()));
  }

  private Mono<ServerWebExchange> handleExchangeWithPassport(final ServerWebExchange serverWebExchange) {
    HttpHeaders httpHeaders = serverWebExchange.getRequest().getHeaders();
    if (!httpHeaders.containsKey(HttpHeaders.AUTHORIZATION)) {
      return Mono.just(serverWebExchange);
    }
    return this.passportService.getPassportData(httpHeaders.getFirst(HttpHeaders.AUTHORIZATION))
        .map(passportData -> {
          ServerHttpRequest.Builder builder = serverWebExchange.getRequest().mutate();
          passportData.forEach((headerName, headerValue) ->
              builder.headers(header -> header.add(PASSPORT_HEADER_PREFIX + headerName, headerValue)));
          return serverWebExchange.mutate()
              .request(builder.build())
              .build();
        });
  }
}
