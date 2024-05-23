package com.pbear.lib.seoul;

import com.pbear.lib.seoul.dto.ResCardSubwayStatsNew;
import com.pbear.lib.seoul.dto.ResSubwayStationMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class SeoulOpenApiClient {
  private static final String stationPath = "/{apiKey}/json/subwayStationMaster/{startIndex}/{endIndex}";
  private static final String cardSubwayStatesNewPath = "/{apiKey}/json/CardSubwayStatsNew/{startIndex}/{endIndex}/{yyyyMMdd}";

  private final WebClient webClient;
  @Value("${seoul-open-api.api-key}")
  private String apiKey;
  @Value("${seoul-open-api.subway.schema:http}")
  private String schema;
  @Value("${seoul-open-api.subway.host:openapi.seoul.go.kr}")
  private String host;
  @Value("${seoul-open-api.subway.port:8088}")
  private int port;

  public Mono<ResSubwayStationMaster> getSeoulStationData(final int startIndex, final int endIndex) {
    return this.webClient
        .method(HttpMethod.GET)
        .uri(uriBuilder -> uriBuilder
            .scheme(this.schema)
            .host(this.host)
            .port(this.port)
            .path(stationPath)
            .build(this.apiKey, startIndex, endIndex))
        .retrieve()
        .bodyToMono(ResSubwayStationMaster.class);
  }

  public Mono<ResCardSubwayStatsNew> getSeoulCardSubwayStats(final int startIndex, final int endIndex, final String yyyyMMdd) {
    return this.webClient
        .method(HttpMethod.GET)
        .uri(uriBuilder -> uriBuilder
            .scheme(this.schema)
            .host(this.host)
            .port(this.port)
            .path(cardSubwayStatesNewPath)
            .build(this.apiKey, startIndex, endIndex, yyyyMMdd))
        .retrieve()
        .bodyToMono(ResCardSubwayStatsNew.class);
  }
}
