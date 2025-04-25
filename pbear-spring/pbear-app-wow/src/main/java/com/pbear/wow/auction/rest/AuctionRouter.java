package com.pbear.wow.auction.rest;

import com.pbear.wow.auction.analyze.TargetItemHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AuctionRouter {
  @Bean
  public RouterFunction<ServerResponse> auctionRoutes(
      final TargetItemHandler targetItemHandler
      ) {
    return RouterFunctions.route()
        .GET("/target/item", targetItemHandler::getTargetItem)
        .POST("/target/item/{id}", targetItemHandler::postTargetItem)
        .DELETE("/target/item/{id}", targetItemHandler::deleteTargetItem)
        .build();
  }
}
