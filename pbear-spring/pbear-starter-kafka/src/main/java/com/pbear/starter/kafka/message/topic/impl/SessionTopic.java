package com.pbear.starter.kafka.message.topic.impl;

import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.topic.MessageTopic;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum SessionTopic implements MessageTopic {
  WEBSOCKET("session.websocket");

  private final String topic;

  @Override
  public String getFullTopic(final MessageType messageType) {
    return messageType.getPrefix() + topic;
  }
}
