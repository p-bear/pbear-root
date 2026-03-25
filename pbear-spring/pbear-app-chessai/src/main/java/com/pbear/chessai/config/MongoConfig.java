package com.pbear.chessai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;

@Configuration
public class MongoConfig {
  @Bean
  public ReactiveGridFsTemplate reactiveGridFsTemplate(final ReactiveMongoDatabaseFactory dbFactory,
                                                       final MappingMongoConverter mappingMongoConverter) {
    return new ReactiveGridFsTemplate(dbFactory, mappingMongoConverter);
  }
}
