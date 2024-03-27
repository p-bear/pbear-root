package com.pbear.subway.business.collect.job;

import com.pbear.lib.common.FieldNotValidException;
import com.pbear.subway.business.collect.data.document.CollectStationJobLog;
import com.pbear.subway.business.collect.data.dto.ResSubwayStationMaster;
import com.pbear.subway.business.collect.service.SeoulSubwayService;
import com.pbear.subway.business.core.data.mapper.StationMapper;
import com.pbear.subway.business.core.service.JobLogService;
import com.pbear.subway.business.core.service.StationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class CollectStationJob {
  private final SeoulSubwayService seoulSubwayService;
  private final StationService stationService;
  private final JobLogService jobLogService;

  /*
   * 1. Seoul OpenData Station 전체 조회
   * 2. save
   * 3. Data Count 및 Save JobLog
   */
  @Scheduled(cron = "@hourly")
  public void collectStation() {
    this.getAllSeoulSubwayStations()
        .map(StationMapper.INSTANCE::toStationDocument)
        .flatMap(this.stationService::saveStation)
        .count()
        .map(CollectStationJobLog::new)
        .flatMap(this.jobLogService::saveJobLog)
        .doOnNext(jobLog -> log.info("collected station, jobLog: {}", jobLog))
        .subscribe();
  }

  /*
   * 1. 1건 조회
   * 2. Response의 totalCount로 다시 전체조회
   */
  private Flux<ResSubwayStationMaster.Station> getAllSeoulSubwayStations() {
    return this.seoulSubwayService.getSeoulStationData(0, 1)
        .doOnNext(a -> System.out.println())
        .filter(ResSubwayStationMaster::isValid)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new FieldNotValidException(ResSubwayStationMaster.class))))
        .map(ResSubwayStationMaster::getSubwayStationMaster)
        .filter(ResSubwayStationMaster.SubwayStationMaster::isValid)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new FieldNotValidException(ResSubwayStationMaster.SubwayStationMaster.class))))
        .flatMap(subwayStationMaster -> this.seoulSubwayService.getSeoulStationData(0, subwayStationMaster.getListTotalCount().intValue()))
        .filter(ResSubwayStationMaster::isValid)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new FieldNotValidException(ResSubwayStationMaster.class))))
        .flatMapIterable(resSubwayStationMaster -> resSubwayStationMaster.getSubwayStationMaster().getRow());
  }
}
