package com.pbear.lib.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
  REQUEST("request."),
  DATA("data."),
  FACT_EVENT("event."),
  DELTA_EVENT("event.");

  private final String prefix;
}
