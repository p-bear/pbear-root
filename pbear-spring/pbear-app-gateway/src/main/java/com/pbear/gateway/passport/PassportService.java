package com.pbear.gateway.passport;

import com.pbear.devtool.Server;
import com.pbear.starter.webflux.DiscoveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class PassportService {
  private static final String PASSPORT_ENDPOINT = "/passport";

  private final WebClient webClient;
  private final DiscoveryService discoveryService;

  @SuppressWarnings("unchecked")
  public Mono<Map<String, String>> getPassportData(final String authorization) {
    return webClient
        .get()
        .uri(UriComponentsBuilder
            .fromUri(this.discoveryService.getTargetServerURI(Server.PBEAR_APP_OAUTH))
            .path(PASSPORT_ENDPOINT)
            .build()
            .toUri())
        .header(HttpHeaders.AUTHORIZATION, authorization)
        .retrieve()
        .onStatus(
            HttpStatusCode::is4xxClientError,
            clientResponse -> Mono.error(new ResponseStatusException(clientResponse.statusCode()))
        )
        .bodyToMono(HashMap.class)
        .flatMapIterable(response -> ((Map<String, Object>) response.get("data")).entrySet())
        .collectMap(Map.Entry::getKey, entry -> String.valueOf(entry.getValue()));
  }
}
