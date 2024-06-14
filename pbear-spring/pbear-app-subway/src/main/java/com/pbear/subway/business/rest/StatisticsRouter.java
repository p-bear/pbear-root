package com.pbear.subway.business.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class StatisticsRouter {
  @Bean
  public RouterFunction<ServerResponse> routeStatistics(final StatisticsHandler statisticsHandler) {
    return RouterFunctions.route()
        .POST("/stations/statistics/subscribe", statisticsHandler::subscribeStatistics)
        .build();
  }
}
