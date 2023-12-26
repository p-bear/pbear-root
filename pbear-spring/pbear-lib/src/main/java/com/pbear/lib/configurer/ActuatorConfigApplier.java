package com.pbear.lib.configurer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.util.List;

public class ActuatorConfigApplier implements EnvironmentPostProcessor {
  @Override
  public void postProcessEnvironment(final ConfigurableEnvironment environment, final SpringApplication application) {
    try {
      List<PropertySource<?>> actuatorPropertySources = new YamlPropertySourceLoader()
          .load("application-actuator.yml", new ClassPathResource("application-actuator.yml"));
      for (PropertySource<?> propertySource : actuatorPropertySources) {
        environment
            .getPropertySources()
            .addFirst(propertySource);
      }
    } catch (IOException e) {
      // no logger loaded yet...
    }
  }
}
