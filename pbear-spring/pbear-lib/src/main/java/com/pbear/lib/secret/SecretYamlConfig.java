package com.pbear.lib.secret;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

@Configuration
@Slf4j
public class SecretYamlConfig {
  @Bean
  @ConditionalOnResource(resources = "classpath:application-secret.yml")
  public YamlPropertiesFactoryBean yamlPropertiesFactoryBean() {
    YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
    bean.setResources(new ClassPathResource("application-secret.yml"));
    return bean;
  }

  @Bean
  @ConditionalOnBean(YamlPropertiesFactoryBean.class)
  public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(final YamlPropertiesFactoryBean yamlPropertiesFactoryBean) {
    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
    Properties secretProperties = yamlPropertiesFactoryBean.getObject();
    if (secretProperties != null) {
      log.info("apply application-secret.yml");
      configurer.setProperties(secretProperties);
    }
    return configurer;
  }
}
