package com.pbear.subway.business.collect.topology;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pbear.lib.event.Message;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.common.MessageDeserializer;
import com.pbear.starter.kafka.message.common.MessageTopic;
import com.pbear.starter.kafka.message.common.DeseiralizerProvider;
import com.pbear.starter.kafka.message.send.KafkaMessagePublisher;
import com.pbear.starter.kafka.message.streams.StoreManager;
import com.pbear.starter.kafka.message.streams.StreamsHelper;
import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.business.collect.data.kafka.ReqCollectStationStatistic;
import com.pbear.subway.business.collect.data.kafka.StationStatisticsData;
import com.pbear.subway.business.common.seoulopenapi.dto.ResCardSubwayStatsNew;
import com.pbear.subway.business.common.seoulopenapi.dto.ResSubwayStationMaster;
import com.pbear.subway.business.common.seoulopenapi.service.SeoulSubwayService;
import com.pbear.subway.business.common.topic.SubwayTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.ParameterizedTypeReference;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CollectStationStatistic {
  private static final MessageType SOURCE_MESSAGE_TYPE = MessageType.REQUEST;
  private static final SubwayTopic SOURCE_TOPIC = SubwayTopic.STATIONS_STATISTICS;

  private final SeoulSubwayService seoulSubwayService;
  private final StoreManager storeManager;
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
        .flatMap(subwayStats -> {
          log.info("collected subwayStats size: {}", subwayStats.size());
          final Map<String, ResSubwayStationMaster.Station> stationDataMap = this.readStations();
          log.info("stationDataMap size: {}", stationDataMap.size());
          return Flux.fromStream(subwayStats.stream()
              .map(subwayStat -> this.mergeStatisticWithStation(subwayStat, stationDataMap)));
        })
        .flatMap(this::publishStationStatistics)
        .onErrorContinue((throwable, o) -> log.error("fail to getStationStatistics, {}", o, throwable));
  }

  private Map<String, ResSubwayStationMaster.Station> readStations() {
    // stationDataMap<stationName::route, station>
    final Map<String, ResSubwayStationMaster.Station> stationDataMap = new HashMap<>();
    try (KeyValueIterator<String, Message<ResSubwayStationMaster.Station>> stationDataIter =
             this.storeManager.getReadOnlyStore(MessageType.DATA, SubwayTopic.STATIONS, new ParameterizedTypeReference<ResSubwayStationMaster.Station>() {})
                 .all()) {

      stationDataIter.forEachRemaining(stationData -> stationDataMap.put(
          MessageTopic.generateKey(stationData.value.data().getStatnNm(), stationData.value.data().getRoute()),
          stationData.value.data()));
    }
    return stationDataMap;
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

  private StationStatisticsData mergeStatisticWithStation(final ResCardSubwayStatsNew.SubwayStats subwayStats,
                                                          final Map<String, ResSubwayStationMaster.Station> stationDataMap) {
    StationStatisticsData.StationStatisticsDataBuilder builder = StationStatisticsData.builder()
        .stationName(subwayStats.getSubStaNm())
        .lineNum(subwayStats.getLineNum())
        .ridePasgrNum(subwayStats.getRidePasgrNum())
        .alignPasgrNum(subwayStats.getAlignPasgrNum())
        .useDate(subwayStats.getUseDt())
        .workDate(subwayStats.getWorkDt());

    String stationKey = MessageTopic.generateKey(subwayStats.getSubStaNm(), subwayStats.getLineNum());
    if (stationDataMap.containsKey(stationKey)) {
      ResSubwayStationMaster.Station station = stationDataMap.get(stationKey);
      builder
          .isMatched(true)
          .stationId(station.getStatnId())
          .latitude(station.getCrdntX())
          .longitude(station.getCrdntY());
    }

    return builder.build();
  }

  private Mono<String> publishStationStatistics(final StationStatisticsData stationStatisticsData) {
    return stationStatisticsData.isMatched() ?
        this.kafkaMessagePublisher.publish(
            MessageType.DATA,
            SubwayTopic.STATIONS_STATISTICS,
            MessageTopic.generateKey(
                stationStatisticsData.getUseDate(),
                stationStatisticsData.getStationId()),
            stationStatisticsData)
        :
        this.kafkaMessagePublisher.publish(
            MessageType.DATA,
            SubwayTopic.STATIONS_STATISTICS.toFailTopic(),
            MessageTopic.generateKey(
                stationStatisticsData.getUseDate(),
                stationStatisticsData.getStationName(),
                stationStatisticsData.getLineNum()),
            stationStatisticsData);
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
