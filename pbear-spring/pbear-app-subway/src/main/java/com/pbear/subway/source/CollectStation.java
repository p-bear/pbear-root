package com.pbear.subway.source;

import com.pbear.lib.event.MessageType;
import com.pbear.lib.seoul.SeoulOpenApiClient;
import com.pbear.lib.seoul.dto.ResSubwayStationMaster;
import com.pbear.starter.kafka.message.send.KafkaMessagePublisher;
import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.topic.SubwayTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectStation {
  private final SeoulOpenApiClient seoulOpenApiClient;
  private final KafkaMessagePublisher kafkaMessagePublisher;

  public Mono<Void> collectStation() {
    Flux.just("")
        .window(Duration.of(1L, ChronoUnit.HOURS))
        .flatMap(x -> this.getAllSeoulSubwayStations())
        .flatMap(station -> this.kafkaMessagePublisher.publish(
            MessageType.DATA,
            SubwayTopic.STATIONS,
            station.getStatnId(),
            station))
        .subscribeOn(Schedulers.newSingle("collectStationScheduler"))
        .subscribe();

    return Mono.empty();
  }

  /*
   * 1. 1건 조회
   * 2. Response의 totalCount로 다시 전체조회
   */
  private Flux<ResSubwayStationMaster.Station> getAllSeoulSubwayStations() {
    return this.seoulOpenApiClient.getSeoulStationData(0, 1)
        .filterWhen(FieldValidator::validate)
        .map(ResSubwayStationMaster::getSubwayStationMaster)
        .filterWhen(FieldValidator::validate)
        .map(subwayStationMaster -> subwayStationMaster.getListTotalCount().intValue())
        .flatMap(totalCount -> this.seoulOpenApiClient.getSeoulStationData(0, totalCount))
        .filterWhen(FieldValidator::validate)
        .flatMapIterable(resSubwayStationMaster -> resSubwayStationMaster.getSubwayStationMaster().getRow());
  }
}
