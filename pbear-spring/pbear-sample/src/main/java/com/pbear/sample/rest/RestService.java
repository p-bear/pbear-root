package com.pbear.sample.rest;

import com.pbear.devtool.Server;
import com.pbear.sample.r2dbc.DevService;
import com.pbear.starter.webflux.data.dto.CommonRestResponse;
import com.pbear.starter.webflux.DiscoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestService {
  private final WebClient webClient;
  private final DiscoveryService discoveryService;
  private final DevService devService;

  public Mono<ServerResponse> echo(final ServerRequest serverRequest) {
    return Mono.just(serverRequest.path())
        .doOnNext(path -> log.info("req path: {}", path))
        .then(ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of("isGood", true))
            .build()));
  }

  public Mono<ServerResponse> db(final ServerRequest serverRequest) {
    return Mono.just(serverRequest)
        .map(request -> Long.parseLong(serverRequest.queryParam("id").orElseThrow()))
        .flatMap(this.devService::getDev)
        .flatMap(dev -> ServerResponse.ok().body(BodyInserters.fromValue(dev)));
  }

  public Mono<ServerResponse> rest(final ServerRequest serverRequest) {
    return Mono.just(serverRequest)
        .doOnNext(request -> log.info(" >> {}", request.path()))
        .doOnNext(request -> log.info("targetURI: {}", discoveryService.getTargetServerURI(Server.PBEAR_SAMPLE)))
        .map(request -> UriComponentsBuilder
            .fromUri(discoveryService.getTargetServerURI(Server.PBEAR_SAMPLE))
            .path("/" + serverRequest.queryParam("path").orElse("echo"))
            .queryParam("id", 34))
        .flatMap(builder -> webClient
            .get()
            .uri(builder.build().toUri())
            .retrieve()
            .bodyToMono(HashMap.class)
            .then(ServerResponse.ok().build()));
  }
}
