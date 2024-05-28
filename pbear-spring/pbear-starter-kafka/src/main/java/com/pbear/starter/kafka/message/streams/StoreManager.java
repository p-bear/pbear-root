package com.pbear.starter.kafka.message.streams;

import com.pbear.lib.event.Message;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.topic.MessageTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(StreamsBuilderFactoryBean.class)
@Slf4j
public class StoreManager {
  private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
  @SuppressWarnings("rawtypes")
  private final Map<String, ReadOnlyKeyValueStore> readOnlyStoreMap = new HashMap<>();

  @SuppressWarnings("unchecked")
  public <V> ReadOnlyKeyValueStore<String, Message<V>> getReadOnlyStore(final MessageType messageType,
                                                                        final MessageTopic topic,
                                                                        final ParameterizedTypeReference<V> valueType) {
    final String storeName = topic.getStoreName(messageType);
    if (this.readOnlyStoreMap.containsKey(storeName)) {
      return this.readOnlyStoreMap.get(storeName);
    }

    KafkaStreams kafkaStreams = this.streamsBuilderFactoryBean.getKafkaStreams();
    if (kafkaStreams == null || valueType == null) {
      throw new RuntimeException("fail to getReadOnlyStore");
    }
    ReadOnlyKeyValueStore<String, Message<V>> readOnlyKeyValueStore = kafkaStreams
        .store(StoreQueryParameters
            .<ReadOnlyKeyValueStore<String, Message<V>>>fromNameAndType(storeName, QueryableStoreTypes.keyValueStore())
            .enableStaleStores());
    this.readOnlyStoreMap.put(storeName, readOnlyKeyValueStore);
    return readOnlyKeyValueStore;
  }
}
