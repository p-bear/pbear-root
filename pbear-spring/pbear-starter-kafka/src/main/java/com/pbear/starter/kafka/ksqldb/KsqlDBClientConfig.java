package com.pbear.starter.kafka.ksqldb;

import io.confluent.ksql.api.client.Client;
import io.confluent.ksql.api.client.ClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "ksqldb.host")
public class KsqlDBClientConfig {
  @Value("${ksqldb.host}")
  private String host;
  @Value("${ksqldb.port:8088}")
  private Integer port;

  @Bean
  public Client defaultClient() {
    return Client.create(ClientOptions.create()
        .setHost(this.host)
        .setPort(this.port));
  }
}
