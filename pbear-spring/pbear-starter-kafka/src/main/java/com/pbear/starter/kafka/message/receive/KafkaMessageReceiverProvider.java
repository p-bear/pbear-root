package com.pbear.starter.kafka.message.receive;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.lib.event.CommonMessage;
import com.pbear.starter.kafka.KafkaPropProvider;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.observation.KafkaReceiverObservation;
import reactor.kafka.receiver.observation.KafkaRecordReceiverContext;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageReceiverProvider {
  private final ObservationRegistry observationRegistry;
  private final KafkaPropProvider kafkaPropProvider;
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Value("${spring.application.name}")
  private String applicationName;

  public <K, V, M extends CommonMessage<V>> Flux<?> executeReceiver(final KafkaReceiverConfig<K, V, M> kafkaReceiverConfig) {
    return this.executeReceiver(kafkaReceiverConfig,
        kafkaReceiver -> kafkaReceiver.receiveAutoAck().concatMap(r -> r));
  }

  public <K, V, M extends CommonMessage<V>> Flux<?> executeReceiver(
      final KafkaReceiverConfig<K, V, M> kafkaReceiverConfig,
      final Function<KafkaReceiver<K, byte[]>, Flux<ConsumerRecord<K, byte[]>>> receiveFunction) {
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
            .<K, byte[]>create(consumerProperties)
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

          // convert record value: byte[] to object
          ConsumerRecord<K, M> downStreamRecord;
          try {
            downStreamRecord = new ConsumerRecord<>(
                record.topic(),
                record.partition(),
                record.offset(),
                record.key(),
                this.objectMapper.readValue(record.value(), new TypeReference<>() {})
            );
          } catch (IOException e) {
            receiverObservation.error(e);
            receiverObservation.stop();
            return Mono.error(e);
          }

          return Mono.just(downStreamRecord)
              .doOnNext(this::logRecord)
              .flatMap(kafkaReceiverConfig.getConsumeMonoFunc())
              .doOnTerminate(receiverObservation::stop)
              .doOnError(receiverObservation::error)
              .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, receiverObservation));
        });
  }

  private void logRecord(final ConsumerRecord<?, ?> record) {
    try {
      log.info("receive message, topic: {}, message: {}", record.topic(), this.objectMapper.writeValueAsString(record.value()));
    } catch (JsonProcessingException e) {
      log.error("fail to log record, ", e);
    }
  }
}
