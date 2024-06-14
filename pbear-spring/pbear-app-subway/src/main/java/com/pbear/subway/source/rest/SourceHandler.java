package com.pbear.subway.source.rest;

import com.pbear.starter.webflux.data.dto.CommonRestResponse;
import com.pbear.subway.source.core.CollectStation;
import com.pbear.subway.source.core.CollectStationStatistics;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class SourceHandler {
  private final CollectStation collectStation;
  private final CollectStationStatistics collectStationStatistics;

  @SuppressWarnings("unused")
  public Mono<ServerResponse> handlePostStations(final ServerRequest serverRequest) {
    return this.collectStation.collectStation()
        .then(ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of())
            .build()));
  }

  public Mono<ServerResponse> handlePostStatistics(final ServerRequest serverRequest) {
    return serverRequest.bodyToMono(ReqCollectStationStatistic.class)
        .flatMap(reqBody -> this.collectStationStatistics
            .collectStationStatistics(reqBody.getTargetDate(), reqBody.getIsForce() != null && reqBody.getIsForce()))
        .flatMap(count -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of("count", count))
            .build()));
  }
}
