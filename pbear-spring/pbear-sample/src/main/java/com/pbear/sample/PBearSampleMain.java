package com.pbear.sample;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan(basePackages = {"com.pbear.lib"})
public class PBearSampleMain {
  public static void main(String[] args) {
    SpringApplication.run(PBearSampleMain.class, args);
  }
}
