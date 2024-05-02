package com.pbear.starter.kafka.message.receive;

import com.pbear.lib.event.CommonMessage;
import com.pbear.lib.event.MessageType;
import com.pbear.starter.kafka.message.KafkaPubSubConfig;
import lombok.Builder;
import lombok.Getter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import reactor.core.publisher.Mono;

import java.util.Properties;
import java.util.function.Function;

@Builder
@Getter
public class KafkaReceiverConfig<K, V, M extends CommonMessage<V>> implements KafkaPubSubConfig {
  private MessageType messageType;
  private String topic;
  private String groupId;
  private Properties additionalProperties;
  private Function<ConsumerRecord<K, M>, Mono<?>> consumeMonoFunc;
  private String handlerName;
}
