package com.pbear.lib.notify;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class NotifyClient {
  @Value("${notify.slack.bot-user-oauth-token}")
  private String botUserOAuthToken;
  @Value("${notify.slack.channel-id}")
  private String channelId;

  private final WebClient webClient;

  public Mono<String> sendMessage(final String message) {
    return this.webClient.mutate()
        .baseUrl("https://slack.com/api/chat.postMessage")
        .defaultHeaders(httpHeaders -> {
          httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
          httpHeaders.set(HttpHeaders.AUTHORIZATION, "Bearer " + botUserOAuthToken);
        })
        .build()
        .post()
        .bodyValue(Map.of("channel", this.channelId, "text", message))
        .retrieve()
        .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>(){})
        .map(unused -> message);
  }
}
