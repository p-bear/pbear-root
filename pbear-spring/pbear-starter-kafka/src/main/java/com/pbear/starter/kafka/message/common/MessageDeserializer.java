package com.pbear.starter.kafka.message.common;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.lib.event.Message;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@RequiredArgsConstructor
public class MessageDeserializer<V> implements Deserializer<Message<V>> {
  private final ObjectMapper objectMapper;
  private final TypeReference<Message<V>> valueType;


  @Override
  public Message<V> deserialize(final String topic, final byte[] bytes) {
    try {
      return this.objectMapper.readValue(bytes, valueType);
    } catch (IOException e) {
      throw new SerializationException("Can't deserialize data  from topic [" + topic + "]", e);
    }
  }
}
