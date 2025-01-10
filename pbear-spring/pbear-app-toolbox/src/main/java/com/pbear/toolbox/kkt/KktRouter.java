package com.pbear.toolbox.kkt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class KktRouter {
  @Bean
  public RouterFunction<ServerResponse> kktRoute(final KktHandler kktHandler) {
    return RouterFunctions.route()
        .POST("/kkt/mini", kktHandler::handlePostMini)
        .POST("/kkt/csv", kktHandler::handlePostCsv)
        .build();
  }
}
