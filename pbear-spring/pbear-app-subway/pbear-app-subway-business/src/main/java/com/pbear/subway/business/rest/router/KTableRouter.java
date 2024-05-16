package com.pbear.subway.business.rest.router;

import com.pbear.subway.business.rest.handler.KTableDataHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class KTableRouter {
  @Bean
  public RouterFunction<ServerResponse> routeKTable(final KTableDataHandler kTableDataHandler) {
    return RouterFunctions.route()
        .GET("/stations", kTableDataHandler::handleGetStations)
        .POST("/stations", kTableDataHandler::handlePostStations)
        .GET("/stations/statistics", kTableDataHandler::handleGetStatistics)
        .POST("/stations/statistics", kTableDataHandler::handlePostStatistics)
        .build();
  }
}
