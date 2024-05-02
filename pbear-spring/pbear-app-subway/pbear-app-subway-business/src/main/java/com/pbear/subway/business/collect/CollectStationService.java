package com.pbear.subway.business.collect;

import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.receive.KafkaReceiverConfig;
import com.pbear.starter.kafka.message.send.KafkaMessagePublisher;
import com.pbear.starter.kafka.message.receive.KafkaMessageReceiverProvider;
import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.business.common.seoulopenapi.dto.ResSubwayStationMaster;
import com.pbear.subway.business.common.seoulopenapi.service.SeoulSubwayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Properties;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectStationService {
  private final KafkaMessageReceiverProvider kafkaMessageReceiverProvider;
  private final KafkaMessagePublisher kafkaMessagePublisher;
  private final SeoulSubwayService seoulSubwayService;

  @EventListener(classes = ApplicationReadyEvent.class)
  public void start() {
    Properties additionalProperties = new Properties();
    additionalProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

    KafkaReceiverConfig<String, String, CommonMessage<String>> receiverConfig = KafkaReceiverConfig
        .<String, String, CommonMessage<String>>builder()
        .messageType(MessageType.REQUEST)
        .topic("subway.stations")
        .additionalProperties(additionalProperties)
        .consumeMonoFunc(this::handleSubwayStationsRequest)
        .handlerName(this.getClass().getSimpleName())
        .build();

    kafkaMessageReceiverProvider.executeReceiver(receiverConfig)
        .onErrorContinue((throwable, o) -> log.error("fail to execute subwayStationsRequest, {}", o, throwable))
        .subscribe();
  }

  private Mono<?> handleSubwayStationsRequest(final ConsumerRecord<String, CommonMessage<String>> record) {
    return Flux.just(record.value())
//        .delayElements(Duration.of(1L, ChronoUnit.HOURS))
        .flatMap(value -> this.getAllSeoulSubwayStations())
        .flatMap(station -> this.kafkaMessagePublisher.publish(
            MessageType.DATA, "subway.stations", null, station))
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
