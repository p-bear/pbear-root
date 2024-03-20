package com.pbear.asset.business.rest.router;

import com.pbear.asset.business.rest.handler.AccountHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AccountRouter {
  @Bean
  public RouterFunction<ServerResponse> accountRoute(final AccountHandler accountHandler) {
    return RouterFunctions.route()
        .GET("/accounts", accountHandler::handleGetAccounts)
        .build();
  }
}
