package com.pbear.lib.properties;

import org.springframework.core.env.PropertySource;

import java.io.IOException;

public class ActuatorPropertiesApplier implements PropertiesApplier {
  @Override
  public PropertySource<?> getTargetProperties() throws IOException {
    return this.getTargetProperties("application-actuator.yml");
  }
}
