package com.pbear.subway.business.collect;

import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.KafkaReceiverConfig;
import com.pbear.starter.kafka.message.KafkaMessagePublisher;
import com.pbear.starter.kafka.message.KafkaMessageReceiverProvider;
import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.business.common.seoulopenapi.dto.ResSubwayStationMaster;
import com.pbear.subway.business.common.seoulopenapi.service.SeoulSubwayService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollectStationService {
  private final KafkaMessageReceiverProvider kafkaMessageReceiverProvider;
  private final KafkaMessagePublisher kafkaMessagePublisher;
  private final SeoulSubwayService seoulSubwayService;

  @EventListener(classes = ApplicationReadyEvent.class)
  public void start() {
    KafkaReceiverConfig<String, String, CommonMessage<String>> receiverConfig = KafkaReceiverConfig
        .<String, String, CommonMessage<String>>builder()
        .messageType(MessageType.REQUEST)
        .topic("subway.stations")
        .consumeMonoFunc(this::handleSubwayStationsRequest)
        .handlerName(this.getClass().getSimpleName())
        .build();

    kafkaMessageReceiverProvider.executeReceiver(receiverConfig)
        .onErrorResume(throwable -> {
          log.error("fail to execute subwayStationsRequest", throwable);
          return Mono.empty();
        })
        .subscribe();
  }

  private Mono<?> handleSubwayStationsRequest(final ConsumerRecord<String, CommonMessage<String>> record) {
    return this.getAllSeoulSubwayStations()
        .doOnNext(station -> this.kafkaMessagePublisher.publish(
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
