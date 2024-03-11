package com.pbear.starter.mongodb;

import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MongoConfig {
  @Value("${mongodb.host}")
  private String mongodbHost;
  @Value("${mongodb.port}")
  private Integer mongodbPort;

  @Bean
  public MongoClient mongoClient() {
    return MongoClients.create(this.createMongoDBConnectionString());
  }

  @Bean
  public MongoReactiveConfig mongoReactiveConfig() {
    return new MongoReactiveConfig();
  }

  private ConnectionString createMongoDBConnectionString() {
    return new ConnectionString(
        "mongodb://"
        + this.mongodbHost
        + ":"
        + this.mongodbPort
        + "/?readPreference=primary&ssl=false"
    );
  }
}
