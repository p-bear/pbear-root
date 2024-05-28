package com.pbear.starter.kafka.ksqldb.reactive;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.Row;
import io.confluent.ksql.api.client.StreamedQueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@ConditionalOnBean(Client.class)
public class ReactiveKsqlDBClient {
  @SuppressWarnings("SpringJavaInjectionPointsAutowiringInspection")
  private final Client ksqldbClient;

  @Value("${ksqldb.query-timeout:10000}")
  private long queryTimeout;

  public Mono<StreamedQueryResult> streamQuery(final String query, final Duration timeout) {
    return Mono.fromFuture(this.ksqldbClient.streamQuery(query))
        .timeout(timeout);
  }

  public Flux<Row> streamQueryData(final String query) {
    return this.streamQuery(query, Duration.of(this.queryTimeout, ChronoUnit.MILLIS))
        .flatMapMany(streamedQueryResult ->  streamedQueryResult);
  }

  // TODO: query and map to clz
  public Mono<List<Row>> executeQuery(final String query, final Duration timeout) {
    return Mono.fromFuture(this.ksqldbClient.executeQuery(query))
        .timeout(timeout);
  }

  public Mono<List<Row>> executeQuery(final String query) {
    return this.executeQuery(query, Duration.of(this.queryTimeout, ChronoUnit.MILLIS));
  }
}
