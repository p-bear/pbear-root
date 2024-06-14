package com.pbear.subway.business.rest;

import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.business.core.PipelineService;
import com.pbear.subway.source.core.CollectStationStatistics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsHandler {
  private final CollectStationStatistics collectStationStatistics;
  private final PipelineService pipelineService;

  public Mono<ServerResponse> subscribeStatistics(final ServerRequest request) {
    return request.bodyToMono(ReqSubscribeStatistics.class)
        .filterWhen(FieldValidator::validate)
        .delayUntil(reqBody -> this.collectStationStatistics.collectStationStatistics(reqBody.getTargetDate(), false))
        .flatMap(reqBody -> this.pipelineService.addStationStatisticPipeline(reqBody.getTargetDate(), reqBody.getSessionId()))
        .then(ServerResponse.ok().build());
  }
}
