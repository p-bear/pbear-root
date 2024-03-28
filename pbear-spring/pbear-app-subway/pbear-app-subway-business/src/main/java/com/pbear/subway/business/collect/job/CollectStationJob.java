package com.pbear.subway.business.collect.job;

import com.pbear.starter.webflux.util.FieldValidator;
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
  @Scheduled(cron = "@midnight")
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
        .filterWhen(FieldValidator::validate)
        .map(ResSubwayStationMaster::getSubwayStationMaster)
        .filterWhen(FieldValidator::validate)
        .map(subwayStationMaster -> subwayStationMaster.getListTotalCount().intValue())
        .flatMap(totalCount -> this.seoulSubwayService.getSeoulStationData(0, totalCount))
        .filterWhen(FieldValidator::validate)
        .flatMapIterable(resSubwayStationMaster -> resSubwayStationMaster.getSubwayStationMaster().getRow());
  }
}
