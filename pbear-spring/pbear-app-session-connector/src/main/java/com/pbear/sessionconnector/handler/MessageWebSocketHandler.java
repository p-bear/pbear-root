package com.pbear.sessionconnector.handler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.devtool.Server;
import com.pbear.lib.event.Message;
import com.pbear.lib.event.MessageType;
import com.pbear.sessionconnector.data.WebsocketEvent;
import com.pbear.starter.kafka.message.common.MessageDeserializer;
import com.pbear.starter.kafka.message.receive.KafkaMessageReceiverProvider;
import com.pbear.starter.kafka.message.receive.KafkaReceiverConfig;
import com.pbear.starter.kafka.message.send.KafkaMessagePublisher;
import com.pbear.starter.kafka.message.topic.impl.SessionTopic;
import com.pbear.starter.kafka.topology.SubTopology;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.PingMessage;
import org.springframework.web.socket.PongMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageWebSocketHandler extends AbstractWebSocketHandler implements SubTopology {
  private static final String WEBSOCKET_MESSAGE_GROUP_ID =
      Server.PBEAR_APP_SESSION_CONNECTOR.getApplicationName() + UUID.randomUUID();

  private final KafkaMessageReceiverProvider kafkaMessageReceiverProvider;
  private final KafkaMessagePublisher kafkaMessagePublisher;
  private final ObjectMapper objectMapper;
  private final ApplicationEventPublisher applicationEventPublisher;

  private final Sinks.Many<DownStreamMessage> messageSource =
      Sinks.many().multicast().onBackpressureBuffer();
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
  }

  private Mono<Sinks.EmitResult> emitRecord(final ConsumerRecord<String, Message<Object>> record) {
    return Mono.just(new DownStreamMessage(record))
        .map(this.messageSource::tryEmitNext)
        .doOnNext(emitResult -> log.info("emit success? {}, key: {}, data: {}",
            emitResult.isSuccess(), record.key(), record.value().data()));
  }

  @Override
  public void afterConnectionEstablished(final WebSocketSession session) {
    if (this.downStreams.containsKey(session.getId())) {
      log.info("duplicate session, sessionId: {}", session.getId());
      return;
    }
    log.info("current subscriberCount: {}", this.messageSource.currentSubscriberCount());
    Disposable downStream = this.messageSource.asFlux()
        .filter(downStreamMessage -> downStreamMessage.getSessionId().equals(session.getId()))
        .flatMap(downStreamMessage -> this.sendMessage(session, downStreamMessage))
        .subscribeOn(Schedulers.boundedElastic())
        .subscribe();
    log.info("add new Session, id: {}", session.getId());
    this.downStreams.put(session.getId(), downStream);
    this.kafkaMessagePublisher.publish(MessageType.DATA, SessionTopic.WEBSOCKET, session.getId(), new WebsocketSessionData(session))
        .subscribe();
  }

  private Mono<Boolean> sendMessage(final WebSocketSession currentSession, final DownStreamMessage downStreamMessage) {
    return Mono.just(downStreamMessage.getMessage().data())
        .flatMap(data -> {
          try {
            currentSession.sendMessage(new TextMessage(this.objectMapper.writeValueAsString(data)));
            return Mono.just(true);
          } catch (IOException e) {
            return Mono.error(e);
          }
        })
        .doOnError(throwable -> log.error("fail to send Message, sessionId: {}", currentSession.getId()))
        .onErrorComplete();
  }

  @Override
  public void handleMessage(@NonNull final WebSocketSession session, @NonNull final WebSocketMessage<?> message) throws Exception {
    if (message instanceof PingMessage) {
      session.sendMessage(new PongMessage());
    } else if (message instanceof TextMessage textMessage) {
      WebsocketEvent websocketEvent = this.objectMapper.readValue(textMessage.getPayload(), WebsocketEvent.class);
      websocketEvent.setWebSocketSession(session);
      this.applicationEventPublisher.publishEvent(websocketEvent);
    }
  }

  @Override
  public void afterConnectionClosed(final WebSocketSession session, @NonNull final CloseStatus status) {
    log.info("session closed, sessionId: {}, closeStatus: {}", session.getId(), status);
    this.handleSessionFinish(session);
  }

  @Override
  public void handleTransportError(final WebSocketSession session, @NonNull final Throwable exception) {
    log.error("session transport error, sessionId: {}", session.getId(), exception);
    this.handleSessionFinish(session);
  }

  private void handleSessionFinish(final WebSocketSession session) {
    if (this.downStreams.containsKey(session.getId())) {
      this.downStreams.get(session.getId()).dispose();
      this.downStreams.remove(session.getId());
    }
    this.kafkaMessagePublisher.publish(MessageType.DATA, SessionTopic.WEBSOCKET, session.getId(), null)
        .subscribe();
  }

  @Getter
  private static class DownStreamMessage {
    private final String sessionId;
    private final Message<Object> message;

    public DownStreamMessage(final ConsumerRecord<String, Message<Object>> record) {
      this.sessionId = record.key();
      this.message = record.value();
    }
  }

  @Getter
  @ToString
  public static class WebsocketSessionData {
    private final String id;
    private final Set<String> tags;
    private final URI uri;
    private final HttpHeaders headers;
    MultiValueMap<String, String> parameters;
    private final Map<String, Object> attributes;
    private final InetSocketAddress localAddress;
    private final InetSocketAddress remoteAddress;

    @SuppressWarnings("all")
    public WebsocketSessionData(final WebSocketSession webSocketSession) {
      this.id = webSocketSession.getId();
      this.uri = webSocketSession.getUri();
      this.attributes = webSocketSession.getAttributes();
      this.headers = webSocketSession.getHandshakeHeaders();
      this.parameters =
          UriComponentsBuilder.fromUri(Objects.requireNonNull(webSocketSession.getUri())).build().getQueryParams();
      this.localAddress = webSocketSession.getLocalAddress();
      this.remoteAddress = webSocketSession.getRemoteAddress();
      this.tags = this.collectTags();
    }

    private Set<String> collectTags() {
      return Stream.concat(this.headers.entrySet().stream(), this.parameters.entrySet().stream())
          .filter(entry -> entry.getKey().equalsIgnoreCase("tag"))
          .flatMap(entry -> entry.getValue().stream())
          .collect(Collectors.toSet());
    }
  }
}
