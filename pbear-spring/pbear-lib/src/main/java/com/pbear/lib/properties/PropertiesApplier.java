package com.pbear.lib.properties;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

interface PropertiesApplier extends EnvironmentPostProcessor {
  @Override
  default void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application){
    try {
      environment
          .getPropertySources()
          .addFirst(this.getTargetProperties());
    } catch (IOException e) {
      // no logger loaded yet...
      e.printStackTrace(System.out);
    }
  }

  PropertySource<?> getTargetProperties() throws IOException;

  default PropertySource<?> getTargetProperties(final String fileName) throws IOException {
    List<PropertySource<?>> secretPropertySources = new YamlPropertySourceLoader()
        .load("classpath:" + fileName, new ClassPathResource(fileName));
    if (secretPropertySources == null || secretPropertySources.isEmpty()) {
      throw new IOException("fail to find " + fileName);
    }
    System.out.println(((OriginTrackedMapPropertySource) secretPropertySources.get(0)).getSource());
    return secretPropertySources.get(0);
  }
}
