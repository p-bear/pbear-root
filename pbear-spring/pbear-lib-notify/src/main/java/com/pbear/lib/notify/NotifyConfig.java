package com.pbear.lib.notify;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration(proxyBeanMethods = false)
@Slf4j
public class NotifyConfig {
  @Bean
  @ConditionalOnMissingBean
  public WebClient notifyWebClient() {
    log.warn("webClient Bean not found -> init with simple notify webClient");
    return WebClient.builder().build();
  }
}
