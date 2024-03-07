package com.pbear.oauth.user;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class PassportController {
  private final ObjectMapper objectMapper;

  @GetMapping("/passport")
  public Object getUserInfo() throws JsonProcessingException {
    String authName = SecurityContextHolder.getContext().getAuthentication().getName();
    return Map.of(
        "result", "success",
        "traceId", MDC.get("traceId"),
        "data", this.objectMapper.readValue(authName, HashMap.class)
    );
  }
}
