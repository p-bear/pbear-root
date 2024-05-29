package com.pbear.sessionconnector.route;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.sessionconnector.data.WebsocketEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;

import java.io.IOException;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SessionRouter {
  private final ObjectMapper objectMapper;

  @EventListener(condition = "#websocketEvent.route == 'get.session'")
  @Async
  public void getSession(final WebsocketEvent websocketEvent) throws IOException {
    TextMessage textMessage = this.toTextMessage(Map.of(
        "sessionId", websocketEvent.getWebSocketSession().getId()));
    websocketEvent.getWebSocketSession().sendMessage(textMessage);
  }

  private TextMessage toTextMessage(final Object body) {
    try {
      return new TextMessage(this.objectMapper.writeValueAsString(body));
    } catch (JsonProcessingException e) {
      log.error("fail to Serialize", e);
      return new TextMessage("{}");
    }
  }
}
