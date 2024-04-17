package com.pbear.subway.business.core.service;

import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.business.core.document.StationStatistics;
import com.pbear.subway.business.core.repository.StationStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class StationStatisticsService {
  private final StationStatisticsRepository stationStatisticsRepository;

  public Mono<StationStatistics> saveStationStatistics(final StationStatistics stationStatistics) {
    return Mono.just(stationStatistics)
        .filterWhen(FieldValidator::validate)
        .flatMap(stationStatisticsRepository::save);
  }
}
