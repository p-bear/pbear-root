package com.pbear.lib.configurer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

@Configuration
@Slf4j
public class YamlConfigurer {
  @Bean
  public YamlPropertiesFactoryBean yamlPropertiesFactoryBean(final ApplicationContext applicationContext) {
    YamlPropertiesFactoryBean bean = new YamlPropertiesFactoryBean();
    List<Resource> resourceList = new ArrayList<>();
    this.addClassPathResourceIfExsist(resourceList, "application-actuator.yml", applicationContext);
    this.addClassPathResourceIfExsist(resourceList, "application-secret.yml", applicationContext);
    bean.setResources(resourceList.toArray(new Resource[0]));
    return bean;
  }

  @Bean
  public PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer(final YamlPropertiesFactoryBean yamlPropertiesFactoryBean) {
    PropertySourcesPlaceholderConfigurer configurer = new PropertySourcesPlaceholderConfigurer();
    Properties properties = yamlPropertiesFactoryBean.getObject();
    if (properties != null) {
      configurer.setProperties(properties);
    }
    return configurer;
  }

  private void addClassPathResourceIfExsist(final List<Resource> resourceList, final String fileName, final ApplicationContext applicationContext) {
    if (applicationContext.getResource("classpath:" + fileName).exists()) {
      resourceList.add(new ClassPathResource(fileName));
      log.info("ClassPath Resource Added: {}", fileName);
    } else {
      log.info("ClassPath Resource not exist, {}", fileName);
    }
  }
}
