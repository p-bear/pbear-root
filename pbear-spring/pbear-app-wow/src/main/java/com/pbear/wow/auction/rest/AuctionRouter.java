package com.pbear.wow.auction.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
public class AuctionRouter {
  @Bean
  public RouterFunction<ServerResponse> auctionRoutes(
      final TargetHandler targetHandler,
      final AnalyzeHandler analyzeHandler
      ) {
    return RouterFunctions.route()
        .GET("/target/item", targetHandler::getTargetItem)
        .POST("/target/item/{id}", targetHandler::postTargetItem)
        .DELETE("/target/item/{id}", targetHandler::deleteTargetItem)
        .GET("/analyze/price/item/{itemId}", analyzeHandler::getAnalyzePriceItemItemId)
        .POST("/analyze/price/item/refresh", analyzeHandler::postAnalyzePriceItemRefresh)
        .build();
  }
}
