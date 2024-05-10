package com.pbear.subway.business.collect.topology;

import com.fasterxml.jackson.core.type.TypeReference;
import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.DeseiralizerProvider;
import com.pbear.starter.kafka.message.streams.StreamsHelper;
import com.pbear.subway.business.common.seoulopenapi.dto.ResSubwayStationMaster;
import com.pbear.subway.business.common.topic.SubwayTopic;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StationTable {
  @Bean
  public GlobalKTable<String, CommonMessage<ResSubwayStationMaster.Station>> dataStationsTable(
      final StreamsHelper streamsHelper,
      final DeseiralizerProvider deseiralizerProvider) {
    return streamsHelper.createCommonMessageKTable(
        MessageType.DATA,
        SubwayTopic.STATIONS,
        deseiralizerProvider.getCommonMessageDeserializer(new TypeReference<>() {}));
  }
}
