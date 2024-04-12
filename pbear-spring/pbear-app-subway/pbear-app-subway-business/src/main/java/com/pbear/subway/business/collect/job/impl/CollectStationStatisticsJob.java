package com.pbear.subway.business.collect.job.impl;

import com.pbear.subway.business.collect.data.document.CollectStationStatisticsJobLog;
import com.pbear.subway.business.collect.data.document.CollectStationStatisticsJobState;
import com.pbear.subway.business.collect.job.AbstractAsyncJob;
import com.pbear.subway.business.collect.service.SeoulSubwayService;
import com.pbear.subway.business.core.service.JobService;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class CollectStationStatisticsJob extends AbstractAsyncJob<CollectStationStatisticsJobState, CollectStationStatisticsJobLog> {
  private final SeoulSubwayService seoulSubwayService;

  public CollectStationStatisticsJob(final JobService jobService, final ObservationRegistry observationRegistry, final SeoulSubwayService seoulSubwayService) {
    super(jobService, observationRegistry);
    this.seoulSubwayService = seoulSubwayService;
  }

  @Override
  protected Class<CollectStationStatisticsJobState> getJobStateClz() {
    return CollectStationStatisticsJobState.class;
  }

  @Override
  protected CollectStationStatisticsJobState defaultJobState() {
    return new CollectStationStatisticsJobState();
  }

  /*
   * flow: https://github.com/p-bear/charts.draw.io/blob/main/subway/jobFlow.drawio.png?raw=true
   */
  @Override
  protected Mono<CollectStationStatisticsJobLog> executeInternal() {
    return null;
  }
}
