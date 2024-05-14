package com.pbear.starter.kafka.message.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.lib.event.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeseiralizerProvider {
  private final ObjectMapper objectMapper;

  public <V> MessageDeserializer<V> getMessageDeserializer(final TypeReference<Message<V>> valueType) {
    return new MessageDeserializer<>(this.objectMapper, valueType);
  }
}
