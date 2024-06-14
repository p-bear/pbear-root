package com.pbear.starter.kafka.ksqldb.reactive;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.Row;
import io.confluent.ksql.api.client.StreamedQueryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(Client.class)
public class ReactiveKsqlDBClient {
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  private final Client ksqldbClient;

  @Value("${ksqldb.query-timeout:10000}")
  private long queryTimeout;

  public StreamQueryBuilder streamQuery(final String query, final Duration timeout) {
    return new StreamQueryBuilder(query, timeout, this.ksqldbClient);
  }

  // TODO: query and map to clz
  public Mono<List<Row>> executeQuery(final String query, final Duration timeout) {
    return Mono.fromFuture(this.ksqldbClient.executeQuery(query))
        .timeout(timeout);
  }

  public Mono<List<Row>> executeQuery(final String query) {
    return this.executeQuery(query, Duration.of(this.queryTimeout, ChronoUnit.MILLIS));
  }

  @Slf4j
  public static class StreamQueryBuilder {
    private final String query;
    private final Duration timeout;
    private final Client ksqldbClient;
    private Map<String, Object> properties;
    private Scheduler scheduler;
    private Function<? super Row, ? extends Publisher<?>> handlerFunction;

    public StreamQueryBuilder(final String query, final Duration timeout, final Client ksqldbClient) {
      if (query == null || timeout == null) {
        throw new IllegalArgumentException("query and timeout cannot be null");
      }
      this.query = query;
      this.timeout = timeout;
      this.ksqldbClient = ksqldbClient;
    }

    @SuppressWarnings("unused")
    public StreamQueryBuilder subscribeOn(final Scheduler scheduler) {
      this.scheduler = scheduler;
      return this;
    }

    public StreamQueryBuilder queryProperties(final Map<String, Object> properties) {
      this.properties = properties;
      return this;
    }

    public StreamQueryBuilder applyHandler(final Function<? super Row, ? extends Publisher<?>> handlerFunction) {
      this.handlerFunction = handlerFunction;
      return this;
    }

    public void subscribe() {
      CompletableFuture<StreamedQueryResult> future = this.properties == null ?
          this.ksqldbClient.streamQuery(query) : this.ksqldbClient.streamQuery(query, this.properties);
      Flux<Row> rowFlux = Mono
          .fromFuture(future)
          .flatMapMany(Flux::from)
          .timeout(this.timeout)
          .onErrorResume(TimeoutException.class, e -> {
            log.info("close query with timeout: {}", this.timeout);
            return Mono.empty();
          });
      Flux<?> handlerFlux = rowFlux;
      if (this.handlerFunction != null) {
        handlerFlux = rowFlux.flatMap(this.handlerFunction);
      }

      Flux<?> scheduledFlux = handlerFlux;
      if (this.scheduler != null) {
        scheduledFlux = scheduledFlux.subscribeOn(this.scheduler);
      }

      scheduledFlux
          .contextWrite(context -> context.putAllMap(MDC.getCopyOfContextMap()))
          .subscribe(new KsqlDBStreamSubscriber(null, null, null, null));
    }
  }
}
