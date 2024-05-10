package com.pbear.starter.kafka.message.receive;

import com.fasterxml.jackson.core.JsonProcessingException;
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
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
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
@Slf4j
public class KafkaMessageReceiverProvider {
  private final ObservationRegistry observationRegistry;
  private final KafkaPropProvider kafkaPropProvider;
  private final ObjectMapper objectMapper = new ObjectMapper();
  @Value("${spring.application.name}")
  private String applicationName;

  public <K, V> Flux<?> executeReceiver(final KafkaReceiverConfig<K, V> kafkaReceiverConfig) {
    return this.executeReceiver(kafkaReceiverConfig,
        kafkaReceiver -> kafkaReceiver.receiveAutoAck().concatMap(r -> r));
  }

  public <K, V> Flux<?> executeReceiver(
      final KafkaReceiverConfig<K, V> kafkaReceiverConfig,
      final Function<KafkaReceiver<K, CommonMessage<V>>, Flux<ConsumerRecord<K, CommonMessage<V>>>> receiveFunction) {
    // properties initialize with default
    Properties consumerProperties = this.kafkaPropProvider.getConsumerProperties(kafkaReceiverConfig.getAdditionalProperties());
    // consumer groupId

    if (!StringUtils.hasText(kafkaReceiverConfig.getGroupId())) {
      throw new IllegalArgumentException("group.id cannot be null");
    }
    consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, this.applicationName + "-" + kafkaReceiverConfig.getGroupId());

    return receiveFunction.apply(KafkaReceiver.create(ReceiverOptions
            .<K, CommonMessage<V>>create(consumerProperties)
            .subscription(Pattern.compile(kafkaReceiverConfig.getTopic().getFullTopic(kafkaReceiverConfig.getMessageType())))
            .withValueDeserializer(kafkaReceiverConfig.getCommonMessageDeserializer())
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

          return Mono.just(record)
              .doOnNext(this::logRecord)
              .flatMap(kafkaReceiverConfig.getConsumeMonoFunc())
              .doOnTerminate(receiverObservation::stop)
              .doOnError(receiverObservation::error)
              .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, receiverObservation))
              .onErrorContinue((throwable, o) -> log.error("executeReceiver fail, {}", o, throwable));
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
