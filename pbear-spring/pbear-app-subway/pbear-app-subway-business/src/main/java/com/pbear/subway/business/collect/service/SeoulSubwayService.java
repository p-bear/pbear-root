package com.pbear.subway.business.collect.service;

import com.pbear.subway.business.collect.data.dto.ResCardSubwayStatsNew;
import com.pbear.subway.business.collect.data.dto.ResSubwayStationMaster;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SeoulSubwayService {
  private final WebClient webClient;
  @Value("${seoul-open-data.api-key}")
  private String apiKey;
  @Value("${seoul-open-data.subway.schema}")
  private String schema;
  @Value("${seoul-open-data.subway.host}")
  private String host;
  @Value("${seoul-open-data.subway.port}")
  private int port;
  @Value("${seoul-open-data.subway.station.path}")
  private String stationPath;
  @Value("${seoul-open-data.subway.cardSubwayStatesNew.path}")
  private String cardSubwayStatesNewPath;

  public Mono<ResSubwayStationMaster> getSeoulStationData(final int startIndex, final int endIndex) {
    return this.webClient
        .method(HttpMethod.GET)
        .uri(uriBuilder -> uriBuilder
            .scheme(this.schema)
            .host(this.host)
            .port(this.port)
            .path(this.stationPath)
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
            .path(this.cardSubwayStatesNewPath)
            .build(this.apiKey, startIndex, endIndex, yyyyMMdd))
        .retrieve()
        .bodyToMono(ResCardSubwayStatsNew.class);
  }
}
