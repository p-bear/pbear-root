package com.pbear.subway.business.collect.topology;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pbear.lib.event.Message;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.common.MessageDeserializer;
import com.pbear.starter.kafka.message.common.DeseiralizerProvider;
import com.pbear.starter.kafka.message.send.KafkaMessagePublisher;
import com.pbear.starter.kafka.message.streams.StreamsHelper;
import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.business.collect.data.kafka.ReqCollectStationStatistic;
import com.pbear.subway.business.common.seoulopenapi.dto.ResCardSubwayStatsNew;
import com.pbear.subway.business.common.seoulopenapi.service.SeoulSubwayService;
import com.pbear.subway.business.common.topic.SubwayTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class StationStatisticSource {
  private static final MessageType SOURCE_MESSAGE_TYPE = MessageType.REQUEST;
  private static final SubwayTopic SOURCE_TOPIC = SubwayTopic.STATIONS_STATISTICS;

  private final SeoulSubwayService seoulSubwayService;
  private final KafkaMessagePublisher kafkaMessagePublisher;

  @Bean
  public KStream<String, Message<ReqCollectStationStatistic>> collectStationStatisticTopology(
      final StreamsHelper streamsHelper,
      final DeseiralizerProvider deseiralizerProvider) {
    // prepare deserializer
    MessageDeserializer<ReqCollectStationStatistic> deserializer =
        deseiralizerProvider.getMessageDeserializer(new TypeReference<>() {});

    // handle stationStatistics with hot Flux (with backpressure)
    Sinks.Many<String> reqHotSource = Sinks.many().multicast().onBackpressureBuffer();
    this.getStationStatistics(reqHotSource)
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();

    return streamsHelper
        .createStreamsBuilderWithStateStore(SOURCE_MESSAGE_TYPE, SOURCE_TOPIC, deserializer)
        .stream(
            SOURCE_TOPIC.getFullTopic(SOURCE_MESSAGE_TYPE),
            Consumed.with(SOURCE_TOPIC.createKeySerdes(), SOURCE_TOPIC.createValueSerdes(deserializer)))
        .processValues(FilterElementDuplicateProcessor::new, SOURCE_TOPIC.getStoreName(SOURCE_MESSAGE_TYPE))
        .peek((key, value) -> reqHotSource.tryEmitNext(key));
  }

  public Flux<?> getStationStatistics(final Sinks.Many<String> hotSource) {
    return hotSource.asFlux()
        .doOnNext(targetDate -> log.info("getStationStatistics start, targetDate: {}", targetDate))
        .flatMap(this::getAllStationStatistics)
        .flatMapIterable(l -> l)
        .flatMap(this::publishStationStatistics)
        .onErrorContinue((throwable, o) -> log.error("fail to getStationStatistics, {}", o, throwable));
  }

  /*
   * 1. 1건 조회
   * 2. Response의 totalCount로 다시 전체조회
   */
  private Mono<List<ResCardSubwayStatsNew.SubwayStats>> getAllStationStatistics(final String targetDate) {
    return this.seoulSubwayService.getSeoulCardSubwayStats(0, 1, targetDate)
        .filterWhen(FieldValidator::validate)
        .map(ResCardSubwayStatsNew::getCardSubwayStatsNew)
        .filterWhen(FieldValidator::validate)
        .map(ResCardSubwayStatsNew.CardSubwayStatsNew::getListTotalCount)
        .map(Long::intValue)
        .flatMap(totalCount -> this.seoulSubwayService.getSeoulCardSubwayStats(0, totalCount, targetDate))
        .filterWhen(FieldValidator::validate)
        .map(ResCardSubwayStatsNew::getCardSubwayStatsNew)
        .map(ResCardSubwayStatsNew.CardSubwayStatsNew::getRow);
  }

  private Mono<String> publishStationStatistics(final ResCardSubwayStatsNew.SubwayStats subwayStats) {
    return this.kafkaMessagePublisher.publish(
        MessageType.DATA,
        SubwayTopic.STATIONS_STATISTICS,
        null,
        subwayStats);
  }


  @Slf4j
  static class FilterElementDuplicateProcessor implements FixedKeyProcessor<String, Message<ReqCollectStationStatistic>, Message<ReqCollectStationStatistic>> {
    private FixedKeyProcessorContext<String, Message<ReqCollectStationStatistic>> context;
    private KeyValueStore<String, Message<ReqCollectStationStatistic>> store;

    @Override
    public void init(final FixedKeyProcessorContext<String, Message<ReqCollectStationStatistic>> context) {
      this.context = context;
      this.store = context
          .getStateStore(SOURCE_TOPIC.getStoreName(SOURCE_MESSAGE_TYPE));
    }

    @Override
    public void process(final FixedKeyRecord<String, Message<ReqCollectStationStatistic>> fixedKeyRecord) {
      Message<ReqCollectStationStatistic> prevValue = store.get(fixedKeyRecord.key());
      store.put(fixedKeyRecord.key(), fixedKeyRecord.value());
      boolean isForce = fixedKeyRecord.value().data().getIsForce() != null && fixedKeyRecord.value().data().getIsForce();
      if (isForce || prevValue == null) {
        this.context.forward(fixedKeyRecord);
      }
    }
  }
}
