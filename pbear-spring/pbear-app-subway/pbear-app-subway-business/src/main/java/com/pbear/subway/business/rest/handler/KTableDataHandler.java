package com.pbear.subway.business.rest.handler;

import com.pbear.lib.event.Message;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.common.EmptyData;
import com.pbear.starter.kafka.message.send.KafkaMessagePublisher;
import com.pbear.starter.kafka.message.streams.StoreManager;
import com.pbear.starter.webflux.data.dto.CommonRestResponse;
import com.pbear.starter.webflux.data.exception.RestException;
import com.pbear.starter.webflux.util.PageParam;
import com.pbear.starter.webflux.util.RestUtil;
import com.pbear.subway.business.collect.data.kafka.ReqCollectStationStatistic;
import com.pbear.subway.business.collect.data.kafka.StationStatisticsData;
import com.pbear.subway.business.common.seoulopenapi.dto.ResSubwayStationMaster;
import com.pbear.subway.business.common.topic.SubwayTopic;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class KTableDataHandler {
  private final StoreManager storeManager;
  private final KafkaMessagePublisher kafkaMessagePublisher;

  public Mono<ServerResponse> handleGetStations(final ServerRequest serverRequest) {
    final String stationId = serverRequest.queryParam("stationId").orElse("");
    final PageParam pageParam = RestUtil.parsePageParameter(serverRequest);

    return Mono.just(this.storeManager.getReadOnlyStore(
            MessageType.DATA,
            SubwayTopic.STATIONS,
            new ParameterizedTypeReference<ResSubwayStationMaster.Station>() {}))
        .flatMapMany(keyValueStore -> {
          if (stationId.isEmpty()) {
            return Mono.just(keyValueStore.all())
                .flatMapMany(recordIter -> Flux.fromStream(RestUtil
                    .applyPage(Stream.generate(recordIter::next).takeWhile(x -> recordIter.hasNext()), pageParam)
                    .map(data -> data.value.data())));
          } else {
            Message<ResSubwayStationMaster.Station> record = keyValueStore.get(stationId);
            if (record == null) {
              return Flux.empty();
            }
            return Flux.just(record.data());
          }
        })
        .collectList()
        .flatMap(stations -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of("stationList", stations))
            .build()));
  }

  @SuppressWarnings("unused")
  public Mono<ServerResponse> handlePostStations(final ServerRequest serverRequest) {
    return Mono.just(new EmptyData())
        .flatMap(data -> this.kafkaMessagePublisher
            .publish(MessageType.REQUEST, SubwayTopic.STATIONS, null, data))
        .flatMap(messageId -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of("messageId", messageId))
            .build()));
  }

  public Mono<ServerResponse> handleGetStatistics(final ServerRequest serverRequest) {
    final String useDate = serverRequest.queryParam("useDate")
        .orElseThrow(() -> new RestException(HttpStatus.BAD_REQUEST, "E400"));
    final PageParam pageParam = RestUtil.parsePageParameter(serverRequest);
    return Mono.justOrEmpty(this.storeManager.getReadOnlyStore(
                MessageType.DATA,
                SubwayTopic.STATIONS_STATISTICS,
                new ParameterizedTypeReference<StationStatisticsData>() {})
            .prefixScan(useDate, new StringSerializer()))
        .filter(KeyValueIterator::hasNext)
        .map(recordIter -> RestUtil
            .applyPage(Stream.generate(recordIter::next).takeWhile(x -> recordIter.hasNext()), pageParam)
            .map(record -> record.value.data())
            .toList())
        .switchIfEmpty(Mono.defer(() -> Mono.just(List.of())))
        .flatMap(dataList -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of("stationStatisticList", dataList))
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
