package com.pbear.sample;

import com.pbear.sample.r2dbc.Dev;
import com.pbear.sample.r2dbc.DevRepository;
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
  public RouterFunction<ServerResponse> echoRoute(final DevRepository devRepository) {
    return RouterFunctions.route()
        .route(
            request -> request.path().startsWith("/echo"),
            request -> {
              log.info(" >> {}", request.path());
              return ServerResponse.ok().build();
            })
        .route(
            request -> request.path().startsWith("/db"),
            request -> ServerResponse.ok().body(
                devRepository.findById(Long.parseLong(request.queryParam("id").orElseThrow())),
                Dev.class))
        .build();
  }

}
