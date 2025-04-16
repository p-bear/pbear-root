package com.pbear.wow;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableScheduling
public class PBearAppWowMain {
  public static void main(String[] args) {
    Hooks.enableAutomaticContextPropagation();
    SpringApplication.run(PBearAppWowMain.class, args);
  }
}
