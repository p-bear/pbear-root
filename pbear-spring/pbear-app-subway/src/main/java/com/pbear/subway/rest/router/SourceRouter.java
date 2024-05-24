package com.pbear.subway.rest.router;

import com.pbear.subway.rest.handler.SourceHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class SourceRouter {
  @Bean
  public RouterFunction<ServerResponse> routeSource(final SourceHandler sourceHandler) {
    return RouterFunctions.route()
        .POST("/stations", sourceHandler::handlePostStations)
        .POST("/stations/statistics", sourceHandler::handlePostStatistics)
        .build();
  }
}
