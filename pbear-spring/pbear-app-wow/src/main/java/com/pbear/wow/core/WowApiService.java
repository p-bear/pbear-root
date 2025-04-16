package com.pbear.wow.core;

import com.pbear.wow.data.dto.GetAuctionsCommoditiesRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class WowApiService {
  private static final String REGION_KR = "kr";
  private static final String NAMESPACE_DYNAMIC_KR = "dynamic-kr";
  private static final String LOCALE_KO_KR = "ko_KR";

  @Value("${blizzard.api-url}")
  private String apiUrl;

  private final BlizzardAuthProvider blizzardAuthProvider;
  private final WebClient defaultWebClient;

  public Mono<GetAuctionsCommoditiesRes> getAuctionsCommodities() {
    return this.sendBlizzardRequest(webClient -> webClient
        .get()
        .uri(uriBuilder -> uriBuilder
            .path("/data/wow/auctions/commodities")
            .queryParam("region", REGION_KR)
            .queryParam("namespace", NAMESPACE_DYNAMIC_KR)
            .queryParam("locale", LOCALE_KO_KR)
            .build())
        .retrieve()
        .bodyToMono(GetAuctionsCommoditiesRes.class));
  }

  private  <T> Mono<T> sendBlizzardRequest(Function<WebClient, Mono<T>> handler) {
    return this.blizzardAuthProvider.getClientToken()
        .map(this::getBlizzardWebClient)
        .flatMap(handler);
  }

  private WebClient getBlizzardWebClient(final String token) {
    return this.defaultWebClient.mutate()
        .baseUrl(apiUrl)
        .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
        .build();
  }
}
