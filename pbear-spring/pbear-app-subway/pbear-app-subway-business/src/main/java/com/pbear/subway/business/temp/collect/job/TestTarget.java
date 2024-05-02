package com.pbear.subway.business.temp.collect.job;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Component
@Slf4j
public class TestTarget implements StationStatisticsConfigurer {
  @Override
  public Flux<LocalDate> getTargetDates() {
    return Flux.just(
        LocalDate.of(2024, 2, 1),
        LocalDate.of(2024, 2, 2));
  }
}
