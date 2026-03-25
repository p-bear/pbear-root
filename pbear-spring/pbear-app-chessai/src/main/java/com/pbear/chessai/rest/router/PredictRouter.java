package com.pbear.chessai.rest.router;

import com.pbear.chessai.rest.handler.PredictHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class PredictRouter {
  @Bean
  public RouterFunction<ServerResponse> predictRoute(final PredictHandler predictHandler) {
    return RouterFunctions.route()
        .POST("/predict", predictHandler::postPredict)
        .build();
  }
}
