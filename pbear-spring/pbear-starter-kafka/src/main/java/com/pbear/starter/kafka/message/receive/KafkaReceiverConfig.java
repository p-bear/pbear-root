package com.pbear.starter.kafka.message.receive;

import com.pbear.lib.event.Message;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.topic.MessageTopic;
import lombok.Builder;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Deserializer;
import reactor.core.publisher.Mono;

import java.util.Properties;
import java.util.function.Function;

@Builder
@Getter
public class KafkaReceiverConfig<K, V> {
  private MessageType messageType;
  private MessageTopic topic;
  private String groupId;
  private Properties additionalProperties;
  private Function<ConsumerRecord<K, Message<V>>, Mono<?>> consumeMonoFunc;
  private Deserializer<Message<V>> messageDeserializer;
  private String handlerName;
}
