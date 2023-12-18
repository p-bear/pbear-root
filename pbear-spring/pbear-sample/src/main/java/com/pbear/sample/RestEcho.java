package com.pbear.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@Slf4j
public class RestEcho {
  @Bean
  public RouterFunction<ServerResponse> echoRoute() {
    return RouterFunctions.route()
        .route(request -> true, request -> {
          log.info(" >> {}", request.path());
          return ServerResponse.ok().build();
        })
        .build();
  }

}
