package com.pbear.starter.kafka.message.common;

import com.pbear.lib.event.MessageType;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageFailTopic implements MessageTopicDecorator {
  private static final String FAIL_TOPIC_SUFFIX = ".fail";
  private final MessageTopic delegate;


  @Override
  public String getFullTopic(final MessageType messageType) {
    return this.delegate.getFullTopic(messageType) + FAIL_TOPIC_SUFFIX;
  }
}
