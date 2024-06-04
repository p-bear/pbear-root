package com.pbear.sessionconnector.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Setter
public class JsonMessage {
  private String route;
  private String messageId;
  private Object data;
  private WebSocketSession webSocketSession;
}
