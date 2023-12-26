package com.pbear.sample;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.util.Set;

@Configuration
@Slf4j
public class RestEcho {
  private static final Set<String> excludePathSet = Set.of(
      "actuator"
  );

  @Bean
  public RouterFunction<ServerResponse> echoRoute() {
    return RouterFunctions.route()
        .route(
            request -> {
              for (String path : excludePathSet) {
                if (request.path().startsWith(path)) {
                  return false;
                }
              }
              return true;
            },
            request -> {
              log.info(" >> {}", request.path());
              return ServerResponse.ok().build();
            })
        .build();
  }

}
