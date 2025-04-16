package com.pbear.wow.core;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Base64;

@Component
@RequiredArgsConstructor
@Slf4j
public class BlizzardAuthProvider {
  @Value("${blizzard.auth.client-id}")
  private String clientId;
  @Value("${blizzard.auth.client-secret}")
  private String clientSecret;
  @Value("${blizzard.oauth-url}")
  private String oauthUrl;

  private final WebClient webClient;

  private final Mono<String> cachedTokenMono = Mono.defer(this::requestClientToken)
      .switchIfEmpty(Mono.error(new RuntimeException("fail to get Blizzard Access Token")))
      // cache: expires - 600초
      .cache(tokenResponse -> Duration.ofSeconds(tokenResponse.expires_in() - 600), throwable -> Duration.ZERO, () -> Duration.ZERO)
      .map(TokenResponse::access_token);


  public Mono<String> getClientToken() {
    return this.cachedTokenMono;
  }

  private Mono<TokenResponse> requestClientToken() {
    return this.webClient
        .post()
        .uri(this.oauthUrl + "/oauth/token")
        .header(HttpHeaders.AUTHORIZATION, this.getBasicAuthHeaderValue(this.clientId, this.clientSecret))
        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED_VALUE)
        .body(BodyInserters.fromFormData("grant_type", "client_credentials"))
        .retrieve()
        .bodyToMono(TokenResponse.class);
  }

  private String getBasicAuthHeaderValue(final String clientId, final String clientSecret) {
    return "Basic " + Base64.getEncoder().encodeToString((clientId + ":" + clientSecret).getBytes());
  }

  record TokenResponse(
      String access_token,
      String token_type,
      long expires_in,
      String sub
  ) {}
}
