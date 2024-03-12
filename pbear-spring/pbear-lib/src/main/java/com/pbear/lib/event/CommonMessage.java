package com.pbear.lib.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CommonMessage<T> {
  private final String id;
  private final MessageType messageType;
  private final String issuer;
  private final long timestamp;
  private final T data;
}
