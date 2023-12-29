package com.pbear.starter.r2dbc;

import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import io.r2dbc.spi.Option;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy;
import org.springframework.data.r2dbc.core.R2dbcEntityOperations;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.data.r2dbc.dialect.MySqlDialect;
import org.springframework.r2dbc.core.DatabaseClient;

@Configuration
@ConditionalOnProperty(name = "r2dbc.database")
@EnableR2dbcAuditing
public class R2dbcConfig {
  @Value("${r2dbc.host}")
  private String host;
  @Value("${r2dbc.port}")
  private Integer port;
  @Value("${r2dbc.user}")
  private String user;
  @Value("${r2dbc.password}")
  private String password;
  @Value("${r2dbc.database}")
  private String database;
  @Value("${r2dbc.initialSize:5}")
  private Integer initialSize;
  @Value("${r2dbc.maxSize:20}")
  private Integer maxSize;

  @Bean
  @ConditionalOnProperty(name = "r2dbc.database")
  public ConnectionFactory connectionFactory() {
    return ConnectionFactories.get(ConnectionFactoryOptions.builder()
        .option(ConnectionFactoryOptions.SSL, true)
        .option(ConnectionFactoryOptions.DRIVER, "pool")
        .option(ConnectionFactoryOptions.PROTOCOL, "mariadb")
        .option(ConnectionFactoryOptions.HOST, this.host)
        .option(ConnectionFactoryOptions.PORT, this.port)
        .option(ConnectionFactoryOptions.USER, this.user)
        .option(ConnectionFactoryOptions.PASSWORD, this.password)
        .option(ConnectionFactoryOptions.DATABASE, this.database)
        .option(Option.valueOf("initialSize"), this.initialSize)
        .option(Option.valueOf("maxSize"), this.maxSize)
        .option(Option.valueOf("validationQuery"), "select 1+1")
        .build());
  }

  @Bean
  @ConditionalOnBean(ConnectionFactory.class)
  public R2dbcEntityOperations r2dbcEntityOperations(final ConnectionFactory connectionFactory) {
    return new R2dbcEntityTemplate(
        DatabaseClient.builder()
            .connectionFactory(connectionFactory)
            .build(),
        new DefaultReactiveDataAccessStrategy(MySqlDialect.INSTANCE));
  }
}
