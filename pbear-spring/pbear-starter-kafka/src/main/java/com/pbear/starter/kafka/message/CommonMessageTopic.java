package com.pbear.starter.kafka.message;

import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.springframework.kafka.support.serializer.JsonSerializer;

public interface CommonMessageTopic {
  String STORE_NAME_PREFIX = "store-";

  String getFullTopic(final MessageType messageType);

  default Serde<String> createKeySerdes() {
    return Serdes.String();
  }

  default <V> Serde<CommonMessage<V>> createValueSerdes(final CommonMessageDeserializer<V> deserializer) {
    assert deserializer != null;
    return Serdes.serdeFrom(new JsonSerializer<>(), deserializer);
  }

  default String getStoreName(final MessageType messageType) {
    return this.STORE_NAME_PREFIX + this.getFullTopic(messageType);
  }
}
