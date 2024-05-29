package com.pbear.sessionconnector.data;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

@Getter
@Setter
public class WebsocketEvent {
  private String route;
  private Object data;
  private WebSocketSession webSocketSession;
}
