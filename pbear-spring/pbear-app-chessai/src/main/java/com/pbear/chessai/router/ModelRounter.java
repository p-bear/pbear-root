package com.pbear.chessai.router;

import com.pbear.chessai.handler.ModelHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class ModelRounter {
  @Bean
  public RouterFunction<ServerResponse> modelRouterFunction(final ModelHandler modelHandler) {
    return RouterFunctions.route()
        .POST("/model/{modelName}", modelHandler::postModel)
        .GET("/model/{modelName}", modelHandler::getModel)
        .build();
  }
}
