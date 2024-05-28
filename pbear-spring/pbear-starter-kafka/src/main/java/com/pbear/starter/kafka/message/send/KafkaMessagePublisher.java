package com.pbear.starter.kafka.message.send;

import com.pbear.lib.event.Message;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.topic.MessageTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class KafkaMessagePublisher {
  private final ApplicationEventPublisher delegate;
  @Value("${spring.application.name}")
  private String applicationName;

  public <T> Mono<String> publish(final MessageType messageType, final MessageTopic topic, final String key, final T data) {
    return Mono.just(UUID.randomUUID().toString())
        .transformDeferredContextual((messageIdMono, contextView) -> messageIdMono
            .map(messageId -> KafkaSendConfig
                .<T>builder()
                .topic(topic)
                .key(key)
                .contextView(contextView)
                .message(new Message<>(
                    messageId,
                    messageType,
                    this.applicationName,
                    new Date().getTime(),
                    data))
                .build()))
        .doOnNext(this.delegate::publishEvent)
        .map(kafkaSendConfig -> kafkaSendConfig.getMessage().id());
  }
}
