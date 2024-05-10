package com.pbear.starter.kafka.topology;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubTopologyExecutor {
  private final ApplicationContext applicationContext;

  @EventListener(classes = ApplicationReadyEvent.class)
  public void executeNode() {
    this.applicationContext.getBeansOfType(SubTopology.class)
        .forEach((name, subTopology) -> {
          log.info("execute node: {}", name);
          subTopology.start();
        });
  }
}
