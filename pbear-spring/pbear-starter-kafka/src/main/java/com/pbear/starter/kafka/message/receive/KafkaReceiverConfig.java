package com.pbear.starter.kafka.message.receive;

import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.CommonMessageTopic;
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
  private CommonMessageTopic topic;
  private String groupId;
  private Properties additionalProperties;
  private Function<ConsumerRecord<K, CommonMessage<V>>, Mono<?>> consumeMonoFunc;
  private Deserializer<CommonMessage<V>> commonMessageDeserializer;
  private String handlerName;
}
