package com.pbear.starter.kafka.message;

import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import lombok.Getter;

@Getter
public class KafkaMessage<T> extends CommonMessage<T> {
  private final transient String topic;
  private final transient String key;

  public KafkaMessage(final String id, final MessageType messageType, final String issuer, final long timestamp,
                       final T data, final String topic, final String key) {
    super(id, messageType, issuer, timestamp, data);
    this.topic = topic;
    this.key = key;
  }
}
