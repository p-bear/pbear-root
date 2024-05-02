package com.pbear.starter.kafka.message.send;

import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.KafkaPubSubConfig;
import lombok.Builder;
import lombok.Getter;
import reactor.util.context.ContextView;

@Builder
@Getter
public class KafkaSendConfig<T> implements KafkaPubSubConfig {
  private String topic;
  private String key;
  private ContextView contextView;
  private CommonMessage<T> commonMessage;

  @Override
  public MessageType getMessageType() {
    return this.commonMessage.messageType();
  }
}
