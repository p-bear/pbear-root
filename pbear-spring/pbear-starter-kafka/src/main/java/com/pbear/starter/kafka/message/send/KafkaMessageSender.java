package com.pbear.starter.kafka.message.send;

import com.pbear.lib.event.Message;
import com.pbear.starter.kafka.KafkaPropProvider;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.observation.KafkaRecordSenderContext;
import reactor.kafka.sender.observation.KafkaSenderObservation;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageSender {
  private final KafkaPropProvider kafkaPropProvider;
  private final ObservationRegistry observationRegistry;

  @Value("${spring.application.name}")
  private String applicationName;
  private KafkaSender<String, Message<?>> kafkaSender;

  @Async
  @EventListener
  public void sendKafkaMessage(final KafkaSendConfig<?> kafkaSendConfig) {
    SenderRecord<String, Message<?>, Integer> senderRecord = this.createSenderRecord(kafkaSendConfig);

    Flux.just(senderRecord)
        .map(record -> KafkaSenderObservation.SENDER_OBSERVATION.start(
            null,
            KafkaSenderObservation.DefaultKafkaSenderObservationConvention.INSTANCE,
            () -> new KafkaRecordSenderContext(senderRecord, this.applicationName, null),
            observationRegistry))
        .contextWrite(context -> context.putAll(kafkaSendConfig.getContextView()))
        .flatMap(senderObservation -> this.kafkaSender.send(Mono.just(senderRecord))
            .doOnNext(result -> log.info("produce success, topic: {}, partition: {}, offset: {}",
                result.recordMetadata().topic(), result.recordMetadata().partition(), result.recordMetadata().offset()))
            .doOnTerminate(senderObservation::stop)
            .doOnError(senderObservation::error)
            .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, senderObservation)))
        .subscribe();
  }

  private SenderRecord<String, Message<?>, Integer> createSenderRecord(final KafkaSendConfig<?> kafkaSendConfig) {
    return SenderRecord.create(
        kafkaSendConfig.getTopic().getFullTopic(kafkaSendConfig.getMessage().messageType()),
        null,
        kafkaSendConfig.getMessage().timestamp(),
        kafkaSendConfig.getKey(),
        kafkaSendConfig.getMessage(),
        0);
  }

  @PostConstruct
  public void init() {
    this.kafkaSender = KafkaSender.create(SenderOptions.create(this.kafkaPropProvider.getProducerProperties()));
  }
}
