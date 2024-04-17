package com.pbear.subway.business.collect.job.station;

import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.business.collect.data.document.CollectStationJobLog;
import com.pbear.subway.business.collect.data.document.CollectStationJobState;
import com.pbear.subway.business.collect.data.dto.ResSubwayStationMaster;
import com.pbear.subway.business.collect.job.AbstractAsyncJob;
import com.pbear.subway.business.collect.service.SeoulSubwayService;
import com.pbear.subway.business.core.data.mapper.StationMapper;
import com.pbear.subway.business.core.service.JobService;
import com.pbear.subway.business.core.service.StationService;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

@Component
public class CollectStationJob extends AbstractAsyncJob<CollectStationJobState, CollectStationJobLog> {
  private final SeoulSubwayService seoulSubwayService;
  private final StationService stationService;

  public CollectStationJob(final JobService jobService, final ObservationRegistry observationRegistry, final SeoulSubwayService seoulSubwayService, final StationService stationService) {
    super(jobService, observationRegistry);
    this.seoulSubwayService = seoulSubwayService;
    this.stationService = stationService;
  }

  @Override
  protected Class<CollectStationJobState> getJobStateClz() {
    return CollectStationJobState.class;
  }

  @Override
  protected CollectStationJobState defaultJobState() {
    return new CollectStationJobState();
  }

  /*
   * flow: https://github.com/p-bear/charts.draw.io/blob/main/subway/jobFlow.drawio.png?raw=true
   */
  @Override
  protected Mono<CollectStationJobLog> executeInternal(final CollectStationJobState collectStationJobState) {
    return this.collectStation()
        .onErrorResume(throwable -> Mono.defer(() -> Mono.just(new CollectStationJobLog(false, -1L, -1L, throwable.getMessage()))))
        .switchIfEmpty(Mono.defer(() -> Mono.just(new CollectStationJobLog(false, -1L, -1L))));
  }


  private Mono<CollectStationJobLog> collectStation() {
    return this.stationService.getAllStations()
        .count()
        .zipWith(this.getAllSeoulSubwayStations()
            .map(StationMapper.INSTANCE::toStationDocument)
            .flatMap(this.stationService::saveStation)
            .count())
        .map(TupleUtils.function((beforeCount, afterCount) -> new CollectStationJobLog(true, beforeCount, afterCount)));
  }

  /*
   * 1. 1건 조회
   * 2. Response의 totalCount로 다시 전체조회
   */
  private Flux<ResSubwayStationMaster.Station> getAllSeoulSubwayStations() {
    return this.seoulSubwayService.getSeoulStationData(0, 1)
        .filterWhen(FieldValidator::validate)
        .map(ResSubwayStationMaster::getSubwayStationMaster)
        .filterWhen(FieldValidator::validate)
        .map(subwayStationMaster -> subwayStationMaster.getListTotalCount().intValue())
        .flatMap(totalCount -> this.seoulSubwayService.getSeoulStationData(0, totalCount))
        .filterWhen(FieldValidator::validate)
        .flatMapIterable(resSubwayStationMaster -> resSubwayStationMaster.getSubwayStationMaster().getRow());
  }
}
