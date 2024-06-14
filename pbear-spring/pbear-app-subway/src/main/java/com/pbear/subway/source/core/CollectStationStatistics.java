package com.pbear.subway.source.core;

import com.pbear.lib.event.MessageType;
import com.pbear.lib.seoul.SeoulOpenApiClient;
import com.pbear.lib.seoul.dto.ResCardSubwayStatsNew;
import com.pbear.starter.kafka.ksqldb.reactive.ReactiveKsqlDBClient;
import com.pbear.starter.kafka.message.send.KafkaMessagePublisher;
import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.topic.SubwayTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CollectStationStatistics {
  private final SeoulOpenApiClient seoulOpenApiClient;
  private final KafkaMessagePublisher kafkaMessagePublisher;
  private final ReactiveKsqlDBClient reactiveKsqlDBClient;

  public Mono<Long> collectStationStatistics(final String targetDate, final boolean isForce) {
    return Mono.just(targetDate)
        .filterWhen(target -> this.shouldExecuteQuery(target, isForce))
        .flatMapMany(this::getAllStationStatistics)
        .flatMap(subwayStats -> this.kafkaMessagePublisher.publish(
            MessageType.DATA,
            SubwayTopic.STATIONS_STATISTICS,
            null,
            subwayStats))
        .onErrorContinue((throwable, o) -> log.error("fail to getStationStatistics, {}", o, throwable))
        .count();
  }

  // targetDate의 처리된 Statistics가 없거나 isForce가 true인 경우
  public Mono<Boolean> shouldExecuteQuery(final String targetDate, final boolean isForce) {
    return reactiveKsqlDBClient.executeQuery("SELECT " +
            "USE_DATE " +
            "FROM SUBWAY_PROC_DISTINCT_STATIONS_STATISTICS_STREAM " +
            "WHERE USE_DATE = '" + targetDate + "' " +
            "LIMIT 1;")
        .map(List::size)
        // data가 없거나 force 요청이거나
        .map(size -> size == 0 || isForce);
  }

  /*
   * 1. 1건 조회
   * 2. Response의 totalCount로 다시 전체조회
   */
  private Flux<ResCardSubwayStatsNew.SubwayStats> getAllStationStatistics(final String targetDate) {
    return this.seoulOpenApiClient.getSeoulCardSubwayStats(0, 1, targetDate)
        .filterWhen(FieldValidator::validate)
        .map(ResCardSubwayStatsNew::getCardSubwayStatsNew)
        .filterWhen(FieldValidator::validate)
        .map(ResCardSubwayStatsNew.CardSubwayStatsNew::getListTotalCount)
        .map(Long::intValue)
        .flatMap(totalCount -> this.seoulOpenApiClient.getSeoulCardSubwayStats(0, totalCount, targetDate))
        .filterWhen(FieldValidator::validate)
        .map(ResCardSubwayStatsNew::getCardSubwayStatsNew)
        .flatMapIterable(ResCardSubwayStatsNew.CardSubwayStatsNew::getRow);
  }
}
