package com.pbear.sessionconnector.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.devtool.Server;
import com.pbear.lib.event.Message;
import com.pbear.lib.event.MessageType;
import com.pbear.sessionconnector.data.DownStreamMessage;
import com.pbear.starter.kafka.message.common.MessageDeserializer;
import com.pbear.starter.kafka.message.receive.KafkaMessageReceiverProvider;
import com.pbear.starter.kafka.message.receive.KafkaReceiverConfig;
import com.pbear.starter.kafka.message.topic.impl.SessionTopic;
import com.pbear.starter.kafka.topology.SubTopology;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageDownStreamHandler implements SubTopology {
  private static final String WEBSOCKET_MESSAGE_GROUP_ID =
      Server.PBEAR_APP_SESSION_CONNECTOR.getApplicationName() + UUID.randomUUID();

  private final KafkaMessageReceiverProvider kafkaMessageReceiverProvider;
  private final ObjectMapper objectMapper;

  @Getter
  private final Sinks.Many<DownStreamMessage> messageSource = Sinks.many().multicast().onBackpressureBuffer();
  private final Map<String, Disposable> downStreams = new ConcurrentHashMap<>();

  @Override
  public void start() {
    Properties consumerProperties = new Properties();
    consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");

    this.kafkaMessageReceiverProvider.executeReceiver(
            KafkaReceiverConfig.<String, Object>builder()
                .messageType(MessageType.DATA)
                .topic(SessionTopic.WEBSOCKET_MESSAGE)
                .additionalProperties(consumerProperties)
                .groupId(WEBSOCKET_MESSAGE_GROUP_ID)
                .handlerName("webSocketMessageHandler")
                .messageDeserializer(new MessageDeserializer<>(objectMapper, new TypeReference<>() {}))
                .consumeMonoFunc(this::emitRecord)
                .build())
        .onErrorContinue((throwable, o) -> log.error("fail to execute MessageWebSocketHandler, {}", o, throwable))
        .subscribe();

    // master subscriber
    this.messageSource.asFlux().subscribe();
  }

  public boolean hasSession(final String sessionId) {
    return this.downStreams.containsKey(sessionId);
  }

  public void putSessionDownStream(final String sessionId, final Disposable downStream) {
    this.downStreams.put(sessionId, downStream);
  }

  public void removeSessionDownStream(final String sessionId) {
    if (this.downStreams.containsKey(sessionId)) {
      this.downStreams.get(sessionId).dispose();
      this.downStreams.remove(sessionId);
      log.info("downStream removed {}", sessionId);
    }
  }

  private Mono<Sinks.EmitResult> emitRecord(final ConsumerRecord<String, Message<Object>> record) {
    return Mono.just(new DownStreamMessage(record))
        .map(this.messageSource::tryEmitNext)
        .filter(Sinks.EmitResult::isFailure)
        .doOnNext(emitResult -> log.info("emit fail: {}, key: {}, data: {}", emitResult.name(), record.key(), record.value().data()));
  }
}
