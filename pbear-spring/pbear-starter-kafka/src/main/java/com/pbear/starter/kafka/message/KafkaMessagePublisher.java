package com.pbear.starter.kafka.message;

import com.pbear.lib.event.MessageType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaMessagePublisher {
  private static final String REQUEST_PREFIX = "request.";
  private static final String EVENT_PREFIX = "event.";

  private final ApplicationEventPublisher delegate;
  @Value("${spring.application.name}")
  private String applicationName;

  public <T> String publish(final MessageType messageType, final String topic, final String key, final T data) {
    String messageId = UUID.randomUUID().toString();
    this.delegate.publishEvent(new KafkaMessage<>(
        messageId,
        messageType,
        this.applicationName,
        new Date().getTime(),
        data,
        this.createFullTopic(messageType, topic),
        key));

    return messageId;
  }

  private String createFullTopic(final MessageType messageType, final String topic) {
    if (messageType == null) {
      throw new RuntimeException("invalid messageType!");
    }
    return switch (messageType) {
      case REQUEST -> REQUEST_PREFIX + topic;
      case FACT_EVENT, DELTA_EVENT -> EVENT_PREFIX + topic;
    };
  }
}
