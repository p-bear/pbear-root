package com.pbear.wow.auction.rest;

import com.pbear.starter.webflux.data.dto.CommonRestResponse;
import com.pbear.wow.auction.analyze.ItemAggregateService;
import com.pbear.wow.data.dto.PostAnalyzePriceItemRefreshReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class AnalyzeHandler {
  private final ItemAggregateService itemAggregateService;

  public Mono<ServerResponse> getAnalyzePriceItemItemId(ServerRequest request) {
    return this.itemAggregateService.getItemPriceHistoryById(Long.parseLong(request.pathVariable("itemId")))
        .collectList()
        .flatMap(itemPriceHistoryDocument -> ServerResponse.ok()
            .bodyValue(CommonRestResponse.builder()
                .data(itemPriceHistoryDocument)
                .build()));
  }

  public Mono<ServerResponse> postAnalyzePriceItemRefresh(ServerRequest request) {
    return request.bodyToMono(PostAnalyzePriceItemRefreshReq.class)
        .flatMap(reqBody -> this.itemAggregateService.refreshItemPriceHistroy()
            .collectList())
        .flatMap(list -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of("itemCount", list.size()))
            .build()));
  }
}
