package com.pbear.sessionconnector.config;

import com.pbear.sessionconnector.handler.MessageWebSocketHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;

@Configuration
@EnableWebSocket
public class WebSocketConfig {
  @Bean
  public WebSocketConfigurer webSocketConfigurer(final MessageWebSocketHandler messageWebSocketHandler) {
    return registry -> registry
        .addHandler(messageWebSocketHandler, "/message").setAllowedOrigins("*");
  }
}
