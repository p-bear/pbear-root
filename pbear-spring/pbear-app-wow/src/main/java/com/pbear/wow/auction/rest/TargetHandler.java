package com.pbear.wow.auction.rest;

import com.pbear.starter.webflux.data.dto.CommonRestResponse;
import com.pbear.wow.auction.analyze.ItemAggregateService;
import com.pbear.wow.auction.analyze.TargetItemDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TargetHandler {
  private final ItemAggregateService itemAggregateService;

  public Mono<ServerResponse> getTargetItem(final ServerRequest request) {
    return this.itemAggregateService.getAllTargetItemDocument()
        .collectList()
        .switchIfEmpty(Mono.just(new ArrayList<>()))
        .flatMap(itemList -> ServerResponse.ok()
            .bodyValue(CommonRestResponse.builder()
                .data(Map.of("itemList", itemList))
                .build()));
  }

  public Mono<ServerResponse> postTargetItem(final ServerRequest request) {
    return Mono.just(new TargetItemDocument(Long.parseLong(request.pathVariable("id"))))
        .flatMap(this.itemAggregateService::saveTargetItemDocument)
        .flatMap(targetItem -> ServerResponse.ok()
            .bodyValue(CommonRestResponse.builder()
                .data(targetItem)
                .build()));
  }

  public Mono<ServerResponse> deleteTargetItem(final ServerRequest request) {
    return Mono.just(new TargetItemDocument(Long.parseLong(request.pathVariable("id"))))
        .flatMap(this.itemAggregateService::deleteTargetItemDocument)
        .flatMap(targetItem -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(targetItem)
            .build()));
  }
}
