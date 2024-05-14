package com.pbear.starter.kafka.message.common;

import com.pbear.lib.event.Message;
import com.pbear.lib.event.MessageType;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonSerializer;

public interface MessageTopic {
  String STORE_NAME_PREFIX = "store-";
  String MESSAGE_KEY_DELIMETER = "::";

  String getFullTopic(final MessageType messageType);

  default MessageTopic toFailTopic() {
    return new MessageFailTopic(this);
  }

  default Serde<String> createKeySerdes() {
    return Serdes.String();
  }

  default <V> Serde<Message<V>> createValueSerdes(final MessageDeserializer<V> deserializer) {
    assert deserializer != null;
    return Serdes.serdeFrom(new JsonSerializer<>(), deserializer);
  }

  default String getStoreName(final MessageType messageType) {
    return this.STORE_NAME_PREFIX + this.getFullTopic(messageType);
  }

  static String generateKey(final String... from) {
    return String.join(MESSAGE_KEY_DELIMETER, from);
  }
}
