package com.pbear.sessionconnector;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import reactor.core.publisher.Hooks;

@SpringBootApplication
public class PBearAppSessionConnectorMain {
  public static void main(String[] args) {
    Hooks.enableAutomaticContextPropagation();
    SpringApplication.run(PBearAppSessionConnectorMain.class, args);
  }
}
