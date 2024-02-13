package com.pbear.sample.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@EnableR2dbcRepositories
@Slf4j
public class RestEcho {
  @Bean
  public RouterFunction<ServerResponse> echoRoute(final RestService restService) {
    return RouterFunctions.route()
        .GET("/echo", restService::echo)
        .GET("/db", restService::db)
        .GET("/rest", restService::rest)
        .build();
  }
}
