package com.pbear.subway.business.collect.service;

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
}
