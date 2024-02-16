package com.pbear.user.rest.router;

import com.pbear.user.rest.handler.UserMainHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class UserMainRouter {
  @Bean
  public RouterFunction<ServerResponse> userMainRoute(final UserMainHandler userMainHandler) {
    return RouterFunctions.route()
        .GET("/main/{id}", userMainHandler::handleGetUserMainId)
        .GET("/main", userMainHandler::handleGetUserMain)
        .POST("/main", userMainHandler::handlePostUserMain)
        .POST("/main/password", userMainHandler::handlePostUserMainPassword)
        .build();
  }
}
