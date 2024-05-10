package com.pbear.starter.kafka.message.streams;

import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.CommonMessageDeserializer;
import com.pbear.starter.kafka.message.CommonMessageTopic;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.Stores;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(StreamsBuilderFactoryBean.class)
@RequiredArgsConstructor
public class StreamsHelper {
  private final StreamsBuilder streamsBuilder;

  public <V> StreamsBuilder createStreamsBuilderWithStateStore(final MessageType messageType,
                                                               final CommonMessageTopic topic,
                                                               final CommonMessageDeserializer<V> deserializer) {
    return this.streamsBuilder
        .addStateStore(
            Stores.keyValueStoreBuilder(
                Stores.inMemoryKeyValueStore(topic.getStoreName(messageType)),
                topic.createKeySerdes(),
                topic.createValueSerdes(deserializer))
        );
  }

  public <V> GlobalKTable<String, CommonMessage<V>> createCommonMessageKTable(final MessageType messageType,
                                                                              final CommonMessageTopic topic,
                                                                              final CommonMessageDeserializer<V> deserializer) {
    return this.streamsBuilder
        .globalTable(
            topic.getFullTopic(messageType),
            Materialized
                .<String, CommonMessage<V>, KeyValueStore<Bytes, byte[]>>as(topic.getStoreName(messageType))
                .withStoreType(Materialized.StoreType.IN_MEMORY)
                .withKeySerde(topic.createKeySerdes())
                .withValueSerde(topic.createValueSerdes(deserializer)));
  }
}
