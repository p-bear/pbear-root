package com.pbear.oauth.user;

import java.time.Instant;

public record UserInfo(
    Long id,
    String mainId,
    Instant creDate,
    Instant modDate
) {}
