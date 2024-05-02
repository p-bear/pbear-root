package com.pbear.starter.kafka.message;

import com.pbear.lib.event.MessageType;

public interface KafkaPubSubConfig {
  MessageType getMessageType();
  String getTopic();

  default String createFullTopic() {
    if (this.getMessageType() == null) {
      throw new RuntimeException("invalid messageType!");
    }
    return this.getMessageType().getPrefix() + this.getTopic();
  }
}
