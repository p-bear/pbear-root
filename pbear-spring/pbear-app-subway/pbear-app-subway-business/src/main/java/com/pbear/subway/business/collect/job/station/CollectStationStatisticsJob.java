package com.pbear.subway.business.collect.job.station;

import com.pbear.lib.common.FieldNotValidException;
import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.business.collect.data.document.CollectStationStatisticsJobLog;
import com.pbear.subway.business.collect.data.document.CollectStationStatisticsJobState;
import com.pbear.subway.business.collect.data.dto.ResCardSubwayStatsNew;
import com.pbear.subway.business.collect.job.AbstractAsyncJob;
import com.pbear.subway.business.collect.job.StationStatisticsConfigurer;
import com.pbear.subway.business.collect.service.SeoulSubwayService;
import com.pbear.subway.business.core.data.mapper.StationStatisticsMapper;
import com.pbear.subway.business.core.document.Station;
import com.pbear.subway.business.core.document.StationStatistics;
import com.pbear.subway.business.core.service.JobService;
import com.pbear.subway.business.core.service.StationService;
import com.pbear.subway.business.core.service.StationStatisticsService;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Component
public class CollectStationStatisticsJob extends AbstractAsyncJob<CollectStationStatisticsJobState, CollectStationStatisticsJobLog> {
  private final ApplicationContext applicationContext;
  private final SeoulSubwayService seoulSubwayService;
  private final StationService stationService;
  private final StationStatisticsService stationStatisticsService;

  public CollectStationStatisticsJob(final JobService jobService, final ObservationRegistry observationRegistry, final ApplicationContext applicationContext, final SeoulSubwayService seoulSubwayService, final StationService stationService, final StationStatisticsService stationStatisticsService) {
    super(jobService, observationRegistry);
    this.applicationContext = applicationContext;
    this.seoulSubwayService = seoulSubwayService;
    this.stationService = stationService;
    this.stationStatisticsService = stationStatisticsService;
  }

  @Override
  protected Class<CollectStationStatisticsJobState> getJobStateClz() {
    return CollectStationStatisticsJobState.class;
  }

  @Override
  protected CollectStationStatisticsJobState defaultJobState() {
    return new CollectStationStatisticsJobState(new ArrayList<>(), new HashMap<>());
  }

  /*
   * flow: https://github.com/p-bear/charts.draw.io/blob/main/subway/jobFlow.drawio.png?raw=true
   */
  @Override
  protected Mono<CollectStationStatisticsJobLog> executeInternal(final CollectStationStatisticsJobState collectStationStatisticsJobState) {
    Flux<StationStatisticsConfigurer> cachedConfigurerFlux = Flux.fromIterable(this.applicationContext.getBeansOfType(StationStatisticsConfigurer.class).values()).cache();
    Flux<Station> cachedStations = this.stationService.getAllStations().cache();
    Mono<Map<String, String>> cachedStationNameMap = cachedStations.collectMap(Station::getName, Station::getId).cache();

    // TODO: skip된 역정보가 있으면, 역 정보 가져오기 job 실행
    return this.getTargetDates(cachedConfigurerFlux)
        .doOnNext(s -> log.info("targetDate(full): {}", s))
        .filter(targetDates -> !collectStationStatisticsJobState.getCollectedStationStatistics().contains(targetDates))
        .doOnNext(s -> log.info("targetDate(execute): {}", s))
        .flatMap(this::getAllResCardSubwayStats)
        .flatMap(subwayStats -> cachedConfigurerFlux
            .flatMap(configurer -> configurer.applyConvert(subwayStats)
                .switchIfEmpty(Mono.defer(() -> Mono.just(subwayStats)))))
        .filterWhen(subwayStats -> this.isExistStation(cachedStations, subwayStats, collectStationStatisticsJobState))
        .flatMap(subwayStats -> this.toStationStatistics(subwayStats, cachedStationNameMap))
        .map(StationStatistics::generateDefaultId)
        .flatMap(this.stationStatisticsService::saveStationStatistics)
        .map(StationStatistics::getUseDate)
        .distinct()
        .collectList()
        .doOnNext(collectStationStatisticsJobState::addCollectedStationStatistic)
        .map(useDateList -> new CollectStationStatisticsJobLog(true, useDateList))
        .onErrorResume(throwable -> Mono.defer(() -> Mono.just(new CollectStationStatisticsJobLog(throwable.toString()))))
        .defaultIfEmpty(new CollectStationStatisticsJobLog(false, new ArrayList<>()));
  }

  private Flux<String> getTargetDates(final Flux<StationStatisticsConfigurer> configurerFlux) {
    return configurerFlux
        .concatMap(StationStatisticsConfigurer::getTargetDates)
        .map(localDate -> localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd")))
        .distinct();
  }

  /*
   * 1. 1건 조회
   * 2. Response의 totalCount로 다시 전체조회
   */
  private Flux<ResCardSubwayStatsNew.SubwayStats> getAllResCardSubwayStats(final String yyyyMMdd) {
    return this.seoulSubwayService.getSeoulCardSubwayStats(0, 1, yyyyMMdd)
        .filterWhen(FieldValidator::validate)
        .map(res -> res.getCardSubwayStatsNew().getListTotalCount().intValue())
        .flatMap(totalCount -> this.seoulSubwayService.getSeoulCardSubwayStats(0, totalCount, yyyyMMdd))
        .filterWhen(FieldValidator::validate)
        .flatMapIterable(res -> res.getCardSubwayStatsNew().getRow())
        .filterWhen(FieldValidator::validate)
        .onErrorContinue(FieldNotValidException.class, (throwable, o) -> log.warn("field not valid -> skip, " + "{}", o));
  }

  private Mono<Boolean> isExistStation(final Flux<Station> stationsFlux,
                                      final ResCardSubwayStatsNew.SubwayStats subwayStats,
                                      final CollectStationStatisticsJobState collectStationStatisticsJobState) {
    return stationsFlux
        .filter(station -> station.isSameStation(subwayStats.getSubStaNm(), subwayStats.getLineNum()))
        .next()
        .map(unused -> true)
        .switchIfEmpty(Mono.defer(() -> {
          log.info("skip stationStats >> date,name,line: [{},{},{}]", subwayStats.getUseDt(), subwayStats.getSubStaNm(), subwayStats.getLineNum());
          collectStationStatisticsJobState.addSkipStation(subwayStats.getUseDt(), subwayStats.getSubStaNm(), subwayStats.getLineNum());
          return Mono.just(false);
        }));
  }

  private Mono<StationStatistics> toStationStatistics(final ResCardSubwayStatsNew.SubwayStats subwayStats,
                                                final Mono<Map<String, String>> stationNameMapMono) {
    return stationNameMapMono
        .map(stationNameMap -> StationStatisticsMapper.INSTANCE
            .toStationStatistics(subwayStats, stationNameMap.get(subwayStats.getSubStaNm())));
  }
}
