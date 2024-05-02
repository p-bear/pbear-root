package com.pbear.starter.kafka.message;

import com.pbear.lib.event.CommonMessage;
import com.pbear.starter.kafka.KafkaPropProvider;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.observation.KafkaReceiverObservation;
import reactor.kafka.receiver.observation.KafkaRecordReceiverContext;

import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class KafkaMessageReceiverProvider {
  private final ObservationRegistry observationRegistry;
  private final KafkaPropProvider kafkaPropProvider;
  @Value("${spring.application.name}")
  private String applicationName;

  public <K, V, M extends CommonMessage<V>> Flux<?> executeReceiver(final KafkaReceiverConfig<K, V, M> kafkaReceiverConfig) {
    return this.executeReceiver(kafkaReceiverConfig,
        kafkaReceiver -> kafkaReceiver.receiveAutoAck().concatMap(r -> r));
  }

  public <K, V, M extends CommonMessage<V>> Flux<?> executeReceiver(
      final KafkaReceiverConfig<K, V, M> kafkaReceiverConfig,
      final Function<KafkaReceiver<K, M>, Flux<ConsumerRecord<K, M>>> receiveFunction) {
    // properties initialize with default
    Properties consumerProperties = this.kafkaPropProvider.getConsumerProperties();
    if (kafkaReceiverConfig.getAdditionalProperties() != null) {
      consumerProperties.putAll(kafkaReceiverConfig.getAdditionalProperties());
    }
    // consumer groupId
    if (kafkaReceiverConfig.getGroupId() != null) {
      consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaReceiverConfig.getGroupId());
    } else {
      consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, this.applicationName);
    }

    return receiveFunction.apply(KafkaReceiver.create(ReceiverOptions
            .<K, M>create(consumerProperties)
            .subscription(Pattern.compile(kafkaReceiverConfig.createFullTopic()))
            .withObservation(this.observationRegistry)))
        .flatMap(record -> {
          Observation receiverObservation = KafkaReceiverObservation.RECEIVER_OBSERVATION.start(
              null,
              KafkaReceiverObservation.DefaultKafkaReceiverObservationConvention.INSTANCE,
              () -> new KafkaRecordReceiverContext(
                  record,
                  kafkaReceiverConfig.getHandlerName() == null ? UUID.randomUUID().toString() : kafkaReceiverConfig.getHandlerName(),
                  "default"),
              observationRegistry);

          return kafkaReceiverConfig.getConsumeMonoFunc().apply(record)
              .doOnTerminate(receiverObservation::stop)
              .doOnError(receiverObservation::error)
              .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, receiverObservation));
        });
  }
}
