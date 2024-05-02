package com.pbear.starter.kafka.message;

import com.pbear.lib.event.CommonMessage;
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
  private final ApplicationEventPublisher delegate;
  @Value("${spring.application.name}")
  private String applicationName;

  public <T> String publish(final MessageType messageType, final String topic, final String key, final T data) {
    String messageId = UUID.randomUUID().toString();
    this.delegate.publishEvent(KafkaSendConfig
        .<T>builder()
        .topic(topic)
        .key(key)
        .commonMessage(new CommonMessage<>(
            messageId,
            messageType,
            this.applicationName,
            new Date().getTime(),
            data))
        .build());

    return messageId;
  }
}
