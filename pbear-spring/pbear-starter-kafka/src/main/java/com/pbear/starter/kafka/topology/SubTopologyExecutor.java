package com.pbear.starter.kafka.topology;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubTopologyExecutor {
  private final ApplicationContext applicationContext;

  @PostConstruct
  public void executeNode() {
    this.applicationContext.getBeansOfType(SubTopology.class)
        .forEach((name, subTopology) -> {
          log.info("execute node: {}", name);
          subTopology.start();
        });
  }
}
