package com.pbear.lib.event;

public record Message<T>(
    String id,
    MessageType messageType,
    String issuer,
    long timestamp,
    T data
) {}
