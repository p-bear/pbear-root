package com.pbear.subway.business.common.topic;

import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.CommonMessageTopic;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SubwayTopic implements CommonMessageTopic {
  STATIONS("subway.stations"),
  STATIONS_STATISTICS("subway.stations.statistics");

  private final String topic;

  @Override
  public String getFullTopic(final MessageType messageType) {
    return messageType.getPrefix() + topic;
  }
}
