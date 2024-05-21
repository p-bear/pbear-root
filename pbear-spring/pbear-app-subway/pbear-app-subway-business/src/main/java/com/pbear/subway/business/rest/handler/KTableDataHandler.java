package com.pbear.subway.business.rest.handler;

import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.common.EmptyData;
import com.pbear.starter.kafka.message.send.KafkaMessagePublisher;
import com.pbear.starter.webflux.data.dto.CommonRestResponse;
import com.pbear.subway.business.collect.data.kafka.ReqCollectStationStatistic;
import com.pbear.subway.business.common.topic.SubwayTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class KTableDataHandler {
  private final KafkaMessagePublisher kafkaMessagePublisher;

  @SuppressWarnings("unused")
  public Mono<ServerResponse> handlePostStations(final ServerRequest serverRequest) {
    return Mono.just(new EmptyData())
        .flatMap(data -> this.kafkaMessagePublisher
            .publish(MessageType.REQUEST, SubwayTopic.STATIONS, null, data))
        .flatMap(messageId -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of("messageId", messageId))
            .build()));
  }

  public Mono<ServerResponse> handlePostStatistics(final ServerRequest serverRequest) {
    return serverRequest.bodyToMono(ReqCollectStationStatistic.class)
        .flatMap(reqBody -> this.kafkaMessagePublisher
            .publish(MessageType.REQUEST, SubwayTopic.STATIONS_STATISTICS, reqBody.getTargetDate(), reqBody))
        .flatMap(messageId -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of("messageId", messageId))
            .build()));
  }
}
