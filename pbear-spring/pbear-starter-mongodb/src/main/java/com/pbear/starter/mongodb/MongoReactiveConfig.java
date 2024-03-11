package com.pbear.starter.mongodb;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;

public class MongoReactiveConfig extends AbstractReactiveMongoConfiguration {
  @Value("${mongodb.database}")
  public String databaseName;

  @Override
  protected String getDatabaseName() {
    return this.databaseName;
  }
}
