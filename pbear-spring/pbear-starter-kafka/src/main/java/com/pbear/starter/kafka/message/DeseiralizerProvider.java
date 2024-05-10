package com.pbear.starter.kafka.message;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.lib.event.CommonMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeseiralizerProvider {
  private final ObjectMapper objectMapper;

  public <V> CommonMessageDeserializer<V> getCommonMessageDeserializer(final TypeReference<CommonMessage<V>> valueType) {
    return new CommonMessageDeserializer<>(this.objectMapper, valueType);
  }
}
