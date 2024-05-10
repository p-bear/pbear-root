package com.pbear.starter.kafka.message;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.lib.event.CommonMessage;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@RequiredArgsConstructor
public class CommonMessageDeserializer<V> implements Deserializer<CommonMessage<V>> {
  private final ObjectMapper objectMapper;
  private final TypeReference<CommonMessage<V>> valueType;


  @Override
  public CommonMessage<V> deserialize(final String topic, final byte[] bytes) {
    try {
      return this.objectMapper.readValue(bytes, valueType);
    } catch (IOException e) {
      throw new SerializationException("Can't deserialize data  from topic [" + topic + "]", e);
    }
  }
}
