package com.pbear.starter.kafka.message;

import com.pbear.starter.kafka.KafkaPropProvider;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaMessageSender {
  private final KafkaPropProvider kafkaPropProvider;
  private KafkaSender<String, KafkaMessage<?>> kafkaSender;

  @Async
  @EventListener
  public void sendKafkaMessage(final KafkaMessage<?> kafkaMessage) {
    this.kafkaSender.send(Mono
            .just(kafkaMessage)
            .map(this::createSenderRecord))
        .doOnNext(result -> log.info("produce success, topic: {}, partition: {}, offset: {}",
            result.recordMetadata().topic(), result.recordMetadata().partition(), result.recordMetadata().offset()))
        .subscribe();
  }

  private SenderRecord<String, KafkaMessage<?>, Integer> createSenderRecord(final KafkaMessage<?> kafkaMessage) {
    return SenderRecord.create(
        kafkaMessage.getTopic(),
        null,
        kafkaMessage.getTimestamp(),
        kafkaMessage.getKey(),
        kafkaMessage,
        0);
  }

  @PostConstruct
  public void init() {
    this.kafkaSender = KafkaSender.create(SenderOptions.create(this.kafkaPropProvider.getProducerProperties()));
  }
}
