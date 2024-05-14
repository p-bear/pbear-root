package com.pbear.subway.business.collect.topology;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pbear.lib.event.Message;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.common.DeseiralizerProvider;
import com.pbear.starter.kafka.message.common.EmptyData;
import com.pbear.starter.kafka.message.receive.KafkaReceiverConfig;
import com.pbear.starter.kafka.message.send.KafkaMessagePublisher;
import com.pbear.starter.kafka.message.receive.KafkaMessageReceiverProvider;
import com.pbear.starter.kafka.topology.SubTopology;
import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.business.common.seoulopenapi.dto.ResSubwayStationMaster;
import com.pbear.subway.business.common.seoulopenapi.service.SeoulSubwayService;
import com.pbear.subway.business.common.topic.SubwayTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectStation implements SubTopology {
  private final KafkaMessageReceiverProvider kafkaMessageReceiverProvider;
  private final KafkaMessagePublisher kafkaMessagePublisher;
  private final SeoulSubwayService seoulSubwayService;
  private final DeseiralizerProvider deseiralizerProvider;

  public void start() {
    Properties additionalProperties = new Properties();
    additionalProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

    KafkaReceiverConfig<String, EmptyData> receiverConfig = KafkaReceiverConfig
        .<String, EmptyData>builder()
        .messageType(MessageType.REQUEST)
        .topic(SubwayTopic.STATIONS)
        .additionalProperties(additionalProperties)
        .consumeMonoFunc(this::handleSubwayStationsRequest)
        .messageDeserializer(this.deseiralizerProvider.getMessageDeserializer(new TypeReference<>() {}))
        .handlerName(this.getClass().getSimpleName())
        .groupId(this.getClass().getSimpleName())
        .build();

    kafkaMessageReceiverProvider.executeReceiver(receiverConfig,
            receiver -> receiver.receiveAutoAck()
                .concatMap(r -> r)
                .doOnNext(record -> log.info("request.subway.stations, offset: {}", record.offset()))
                .window(Duration.of(1L, ChronoUnit.HOURS))
                .flatMap(Flux::next)
        )
        .onErrorContinue((throwable, o) -> log.error("fail to execute CollectStationNode, {}", o, throwable))
        .subscribe();
  }

  private Mono<?> handleSubwayStationsRequest(final ConsumerRecord<String, Message<EmptyData>> record) {
    return Flux.just(record.value())
        .publishOn(Schedulers.single())
        .flatMap(value -> this.getAllSeoulSubwayStations())
        .flatMap(station -> this.kafkaMessagePublisher.publish(
            MessageType.DATA,
            SubwayTopic.STATIONS,
            station.getStatnId(),
            station))
        .collectList();
  }

  /*
   * 1. 1건 조회
   * 2. Response의 totalCount로 다시 전체조회
   */
  private Flux<ResSubwayStationMaster.Station> getAllSeoulSubwayStations() {
    return this.seoulSubwayService.getSeoulStationData(0, 1)
        .filterWhen(FieldValidator::validate)
        .map(ResSubwayStationMaster::getSubwayStationMaster)
        .filterWhen(FieldValidator::validate)
        .map(subwayStationMaster -> subwayStationMaster.getListTotalCount().intValue())
        .flatMap(totalCount -> this.seoulSubwayService.getSeoulStationData(0, totalCount))
        .filterWhen(FieldValidator::validate)
        .flatMapIterable(resSubwayStationMaster -> resSubwayStationMaster.getSubwayStationMaster().getRow());
  }
}
