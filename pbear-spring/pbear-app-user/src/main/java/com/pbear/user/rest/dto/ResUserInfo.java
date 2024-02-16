package com.pbear.user.rest.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ResUserInfo(
    Long id,
    String mainId,
    LocalDateTime creDate,
    LocalDateTime modDate
) {}
