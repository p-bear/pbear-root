package com.pbear.subway.business.core;

import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.ksqldb.reactive.ReactiveKsqlDBClient;
import com.pbear.starter.kafka.message.send.KafkaMessagePublisher;
import com.pbear.starter.kafka.message.topic.impl.SessionTopic;
import com.pbear.subway.business.data.kafka.StationStatisticsJoinData;
import io.confluent.ksql.api.client.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PipelineService {
  private final ReactiveKsqlDBClient reactiveKsqlDBClient;
  private final KafkaMessagePublisher kafkaMessagePublisher;

  public Mono<?> addStationStatisticPipeline(final String targetDate, final String sessionId) {
    this.reactiveKsqlDBClient.streamQuery(this.createStationStatisticsQuery(targetDate), Duration.ofSeconds(3L))
        .queryProperties(Map.of("auto.offset.reset", "earliest"))
        .applyHandler(row -> this.createPipeline(row, sessionId))
        .subscribe();
    return Mono.just(sessionId);
  }

  private String createStationStatisticsQuery(final String targetDate) {
    return """
        SELECT
          ID,
          NAME,
          LINE_NUM,
          RIDE_PASGR_NUM,
          ALIGHT_PASGR_NUM,
          USE_DATE,
          LATITUDE,
          LONGITUDE
        FROM SUBWAY_JOIN_STATION_STATISTICS_SUCCESS_STREAM
        WHERE USE_DATE = '{targetDate}'
        EMIT CHANGES;"""
        .replace("{targetDate}", targetDate);
  }

  private Mono<?> createPipeline(final Row row, final String sessionId) {
    return Mono.just(new StationStatisticsJoinData(row))
        .flatMap(data -> this.kafkaMessagePublisher.publish(
            MessageType.DATA,
            SessionTopic.WEBSOCKET_MESSAGE,
            sessionId,
            data));
  }
}
