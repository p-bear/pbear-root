package com.pbear.subway.business.temp.collect.job;

import com.pbear.subway.business.temp.collect.job.station.CollectStationJob;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class X {
  private final CollectStationJob collectStationJob;
  private final ObservationRegistry observationRegistry;

//  @EventListener
  public void init(ApplicationReadyEvent event) {
    Observation observation = Observation.start("xxxx", this.observationRegistry);
    new Thread(() -> Mono.just("==================== xxxx ===========================")
        .doOnNext(log::info)
        .flatMap(unused -> this.collectStationJob.execute())
        .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, observation))
        .subscribe(), "X").start();
  }
}
