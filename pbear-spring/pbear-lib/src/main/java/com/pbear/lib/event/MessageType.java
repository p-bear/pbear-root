package com.pbear.lib.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MessageType {
  REQUEST("request."),
  DATA("data."),
  FACT_EVENT("event.fact"),
  DELTA_EVENT("event.delta"),
  BYPASS("bypass.");

  private final String prefix;
}
