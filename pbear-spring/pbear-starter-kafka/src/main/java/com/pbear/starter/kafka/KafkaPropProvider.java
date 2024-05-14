package com.pbear.starter.kafka;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndContinueExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Component
public class KafkaPropProvider {
  @Value("${kafka.bootstrap.servers}")
  private String bootstrapServers;
  @Value("${spring.application.name}")
  private String applicationName;
  @Value("${spring.profiles.active}")
  private String activeProfile;
  @Value("${kafka.streams.instance-id:}")
  private String streamInstanceId;
  @Value("${kafka.streams.state.dir:}")
  private String streamsStateDir;

  public Properties getProducerProperties() {
    Properties properties = new Properties();
    properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
    properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
    properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
    properties.put("spring.json.add.type.headers", false);
    return properties;
  }

  public Properties getProducerProperties(final Properties additionalProperties) {
    Properties properties = this.getProducerProperties();
    if (additionalProperties != null) {
      properties.putAll(additionalProperties);
    }
    return properties;
  }

  public Properties getConsumerProperties() {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return properties;
  }

  public Properties getConsumerProperties(final Properties additionalProperties) {
    Properties properties = this.getConsumerProperties();
    if (additionalProperties != null) {
      properties.putAll(additionalProperties);
    }
    return properties;
  }

  public Map<String, Object> getStreamsProperties() {
    Map<String, Object> properties = new HashMap<>();
    properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
    properties.put(StreamsConfig.APPLICATION_ID_CONFIG, this.applicationName + "-" + this.activeProfile);
    properties.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, LogAndContinueExceptionHandler.class);    // Exception 발생한 레코드 스킵 후 계속 진행
//    properties.put(StreamsConfig.APPLICATION_SERVER_CONFIG, serviceRegistryAccessor.getHostInfo());
    properties.put(StreamsConfig.NUM_STANDBY_REPLICAS_CONFIG, 1);
    properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 1000);
    properties.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 3);

    if (!this.streamsStateDir.isEmpty()) {
      properties.put(StreamsConfig.STATE_DIR_CONFIG, this.streamsStateDir);
    }

    try (Serde<String> keySerde = Serdes.String()) {
      properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, keySerde.getClass());
    }

    if (!this.streamInstanceId.isEmpty()) {
      properties.put(ConsumerConfig.GROUP_INSTANCE_ID_CONFIG, this.streamInstanceId);
    }
    properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 60000);  // Timeout for streams offline by brokers

    return properties;
  }
}
