package com.pbear.starter.kafka.message.streams;

import com.pbear.starter.kafka.KafkaPropProvider;
import org.apache.kafka.streams.kstream.GlobalKTable;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

@Configuration
@Conditional(KafkaStreamsConfig.KStreamOrKTable.class)
public class KafkaStreamsConfig {
  @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
  public KafkaStreamsConfiguration kafkaStreamsConfiguration(final KafkaPropProvider kafkaPropProvider) {
    return new KafkaStreamsConfiguration(kafkaPropProvider.getStreamsProperties());
  }

  @Bean
  public StreamsBuilderFactoryBean streamsBuilderFactoryBean(final KafkaStreamsConfiguration kafkaStreamsConfiguration) {
    return new StreamsBuilderFactoryBean(kafkaStreamsConfiguration);
  }

  @SuppressWarnings("unused")
  static class KStreamOrKTable extends AnyNestedCondition {
    public KStreamOrKTable() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }

    @ConditionalOnBean(KStream.class)
    static class KStreamExist {}

    @ConditionalOnBean(KTable.class)
    static class KTableExist {}

    @ConditionalOnBean(GlobalKTable.class)
    static class GlobalKTableExist {}

    @ConditionalOnBean(ReadOnlyKeyValueStore.class)
    static class ReadOnlyKeyValueStoreExist {}
  }
}
