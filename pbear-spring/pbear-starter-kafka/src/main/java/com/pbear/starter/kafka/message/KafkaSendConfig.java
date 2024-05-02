package com.pbear.starter.kafka.message;

import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class KafkaSendConfig<T> implements KafkaPubSubConfig {
  private String topic;
  private String key;
  private CommonMessage<T> commonMessage;

  @Override
  public MessageType getMessageType() {
    return this.commonMessage.messageType();
  }
}
