package com.pbear.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories;
import reactor.core.publisher.Hooks;

@SpringBootApplication
@EnableR2dbcRepositories
public class PBearSampleMain {
  public static void main(String[] args) {
    Hooks.enableAutomaticContextPropagation();
    SpringApplication.run(PBearSampleMain.class, args);
  }
}
