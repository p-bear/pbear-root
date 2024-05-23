package com.pbear.subway.topic;

import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.common.MessageTopic;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SubwayTopic implements MessageTopic {
  STATIONS("subway.stations"),
  STATIONS_STATISTICS("subway.stations.statistics");

  private final String topic;

  @Override
  public String getFullTopic(final MessageType messageType) {
    return messageType.getPrefix() + topic;
  }
}
