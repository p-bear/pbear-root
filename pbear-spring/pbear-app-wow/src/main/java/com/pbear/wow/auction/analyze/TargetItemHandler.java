package com.pbear.wow.auction.analyze;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TargetItemHandler {
  private final TargetItemRepository targetItemRepository;

  public Mono<ServerResponse> getTargetItem(final ServerRequest request) {
    return this.targetItemRepository.findAll()
        .collectList()
        .switchIfEmpty(Mono.just(new ArrayList<>()))
        .flatMap(itemList -> ServerResponse.ok()
            .bodyValue(Map.of("itemList", itemList)));
  }

  public Mono<ServerResponse> postTargetItem(final ServerRequest request) {
    return Mono.just(new TargetItemDocument(Long.parseLong(request.pathVariable("id"))))
        .flatMap(this.targetItemRepository::save)
        .flatMap(targetItem -> ServerResponse.ok().bodyValue(targetItem));
  }

  public Mono<ServerResponse> deleteTargetItem(final ServerRequest request) {
    return Mono.just(new TargetItemDocument(Long.parseLong(request.pathVariable("id"))))
        .flatMap(this.targetItemRepository::delete)
        .then(ServerResponse.ok().build());
  }
}
