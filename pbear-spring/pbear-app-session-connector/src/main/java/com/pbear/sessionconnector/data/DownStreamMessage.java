package com.pbear.sessionconnector.data;

import com.pbear.lib.event.Message;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;

@Getter
public class DownStreamMessage {
  private final String sessionId;
  private final Message<Object> message;

  public DownStreamMessage(final ConsumerRecord<String, Message<Object>> record) {
    this.sessionId = record.key();
    this.message = record.value();
  }
}
