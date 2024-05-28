package com.pbear.starter.kafka.message.send;

import com.pbear.lib.event.Message;
import com.pbear.starter.kafka.message.topic.MessageTopic;
import lombok.Builder;
import lombok.Getter;
import reactor.util.context.ContextView;

@Builder
@Getter
public class KafkaSendConfig<T> {
  private MessageTopic topic;
  private String key;
  private ContextView contextView;
  private Message<T> message;
}
