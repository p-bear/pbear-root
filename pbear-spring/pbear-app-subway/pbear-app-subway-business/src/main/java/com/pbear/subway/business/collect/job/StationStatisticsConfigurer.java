package com.pbear.subway.business.collect.job;

import com.pbear.subway.business.collect.data.dto.ResCardSubwayStatsNew;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

public interface StationStatisticsConfigurer {
  default Flux<LocalDate> getTargetDates() {
    return Flux.empty();
  }

  default Mono<ResCardSubwayStatsNew.SubwayStats> applyConvert(final ResCardSubwayStatsNew.SubwayStats origin) {
    return Mono.just(origin);
  }
}
