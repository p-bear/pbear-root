package com.pbear.lib.event;

public record CommonMessage<T>(
    String id,
    MessageType messageType,
    String issuer,
    long timestamp,
    T data
) {}
