package com.pbear.subway.business.collect.topology;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.CommonMessageDeserializer;
import com.pbear.starter.kafka.message.DeseiralizerProvider;
import com.pbear.starter.kafka.message.streams.StreamsHelper;
import com.pbear.subway.business.collect.data.kafka.ReqCollectStationStatistic;
import com.pbear.subway.business.common.topic.SubwayTopic;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.processor.api.FixedKeyProcessor;
import org.apache.kafka.streams.processor.api.FixedKeyProcessorContext;
import org.apache.kafka.streams.processor.api.FixedKeyRecord;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CollectStationStatistic {
  private static final MessageType SOURCE_MESSAGE_TYPE = MessageType.REQUEST;
  private static final SubwayTopic SOURCE_TOPIC = SubwayTopic.STATIONS_STATISTICS;

  @Bean
  public KStream<String, CommonMessage<ReqCollectStationStatistic>> collectStationStatisticTopology(
      final StreamsHelper streamsHelper,
      final DeseiralizerProvider deseiralizerProvider) {
    CommonMessageDeserializer<ReqCollectStationStatistic> deserializer =
        deseiralizerProvider.getCommonMessageDeserializer(new TypeReference<>() {});

    return streamsHelper
        .createStreamsBuilderWithStateStore(SOURCE_MESSAGE_TYPE, SOURCE_TOPIC, deserializer)
        .stream(
            SOURCE_TOPIC.getFullTopic(SOURCE_MESSAGE_TYPE),
            Consumed.with(SOURCE_TOPIC.createKeySerdes(), SOURCE_TOPIC.createValueSerdes(deserializer)))
        .processValues(FilterElementDuplicateProcessor::new, SOURCE_TOPIC.getStoreName(SOURCE_MESSAGE_TYPE))
        .peek((s, reqCollectStationStatisticCommonMessage) -> {
          // TODO: station KTable JOIN Process
        });
  }

  @Slf4j
  static class FilterElementDuplicateProcessor implements FixedKeyProcessor<String, CommonMessage<ReqCollectStationStatistic>, CommonMessage<ReqCollectStationStatistic>> {
    private FixedKeyProcessorContext<String, CommonMessage<ReqCollectStationStatistic>> context;
    private KeyValueStore<String, CommonMessage<ReqCollectStationStatistic>> store;

    @Override
    public void init(final FixedKeyProcessorContext<String, CommonMessage<ReqCollectStationStatistic>> context) {
      this.context = context;
      this.store = context
          .getStateStore(SOURCE_TOPIC.getStoreName(SOURCE_MESSAGE_TYPE));
    }

    @Override
    public void process(final FixedKeyRecord<String, CommonMessage<ReqCollectStationStatistic>> fixedKeyRecord) {
      CommonMessage<ReqCollectStationStatistic> prevValue = store.get(fixedKeyRecord.key());
      store.put(fixedKeyRecord.key(), fixedKeyRecord.value());
      boolean isForce = fixedKeyRecord.value().data().getIsForce() != null && fixedKeyRecord.value().data().getIsForce();
      if (isForce || prevValue == null) {
        this.context.forward(fixedKeyRecord);
      }
    }
  }
}
