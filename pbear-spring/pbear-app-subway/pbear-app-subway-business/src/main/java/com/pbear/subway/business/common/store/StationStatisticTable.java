package com.pbear.subway.business.common.store;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pbear.lib.event.Message;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.common.DeseiralizerProvider;
import com.pbear.starter.kafka.message.streams.StreamsHelper;
import com.pbear.subway.business.collect.data.kafka.StationStatisticsData;
import com.pbear.subway.business.common.topic.SubwayTopic;
import org.apache.kafka.streams.kstream.KTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StationStatisticTable {
  @Bean
  public KTable<String, Message<StationStatisticsData>> dataStationStatisticTable(
      final StreamsHelper streamsHelper,
      final DeseiralizerProvider deseiralizerProvider) {
    return streamsHelper.createMessageKTable(
        MessageType.DATA,
        SubwayTopic.STATIONS_STATISTICS,
        deseiralizerProvider.getMessageDeserializer(new TypeReference<>() {}));
  }
}
