package com.pbear.subway.business;

import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.send.KafkaMessagePublisher;
import com.pbear.starter.kafka.message.streams.StoreManager;
import com.pbear.starter.kafka.message.EmptyData;
import com.pbear.subway.business.collect.data.kafka.ReqCollectStationStatistic;
import com.pbear.subway.business.common.seoulopenapi.dto.ResSubwayStationMaster;
import com.pbear.subway.business.common.topic.SubwayTopic;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequiredArgsConstructor
public class TT {
  private final KafkaMessagePublisher kafkaMessagePublisher;
  private final StoreManager storeManager;

  @PostConstruct
  public void init() {

  }

  @GetMapping("/xx")
  public Mono<?> xx() {
    return Mono.just(new EmptyData())
        .flatMap(data -> this.kafkaMessagePublisher
            .publish(MessageType.REQUEST, SubwayTopic.STATIONS, null, data))
        .map(s -> Map.of("", s));
  }

  @GetMapping("/yy")
  public Mono<?> yy(@RequestParam final String a, @RequestParam final boolean b) {
    return Mono.just(new ReqCollectStationStatistic(b))
        .flatMap(data -> this.kafkaMessagePublisher
            .publish(MessageType.REQUEST, SubwayTopic.STATIONS_STATISTICS, a, data))
        .map(s -> Map.of("", s));
  }

  @GetMapping("/zz")
  public Mono<?> zz(@RequestParam final String a) {
    return Mono.just(a)
        .map(s -> storeManager.getReadOnlyStore(MessageType.DATA,
                SubwayTopic.STATIONS,
                new ParameterizedTypeReference<CommonMessage<ResSubwayStationMaster.Station>>() {})
            .get(s));
  }

  @GetMapping("/ww")
  public Mono<?> ww(@RequestParam final String a) {
    return Mono.just(a)
        .map(s -> storeManager.getReadOnlyStore(
            MessageType.REQUEST,
            SubwayTopic.STATIONS_STATISTICS,
                new ParameterizedTypeReference<CommonMessage<ReqCollectStationStatistic>>() {})
            .get(s));
  }
}
