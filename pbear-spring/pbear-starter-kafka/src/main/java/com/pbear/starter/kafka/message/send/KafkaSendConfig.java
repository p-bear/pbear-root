package com.pbear.starter.kafka.message.send;

import com.pbear.lib.event.CommonMessage;
import com.pbear.starter.kafka.message.CommonMessageTopic;
import lombok.Builder;
import lombok.Getter;
import reactor.util.context.ContextView;

@Builder
@Getter
public class KafkaSendConfig<T> {
  private CommonMessageTopic topic;
  private String key;
  private ContextView contextView;
  private CommonMessage<T> commonMessage;
}
