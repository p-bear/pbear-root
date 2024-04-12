package com.pbear.subway.business.collect.job;

import com.pbear.subway.business.core.document.JobLog;
import com.pbear.subway.business.core.document.JobState;
import com.pbear.subway.business.core.service.JobService;
import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

@RequiredArgsConstructor
public abstract class AbstractAsyncJob<S extends JobState, L extends JobLog> implements AsyncJob<Boolean> {
  private final JobService jobService;
  private final ObservationRegistry observationRegistry;
  private final Logger log = LoggerFactory.getLogger(this.getClass());

  /*
   * flow: https://github.com/p-bear/charts.draw.io/blob/main/subway/jobFlow.drawio.png?raw=true
   * 1. JobState 확인
   * 2. JobState -> RUNNING
   * 3. Job 수행
   * 4. JobLog 생성
   * 5. JobState -> STANDBY
   * 6. return jobLog.isSuccess()
   */
  @Override
  public Mono<Boolean> execute() {
    Observation observation = Observation
        .createNotStarted(this.getClass().getCanonicalName(), this.observationRegistry)
        .start();
    return this.jobService.getJobState(this.getJobStateClz())
        .defaultIfEmpty(this.defaultJobState())
        .filter(this.jobService::isJobExecutable)
        .flatMap(jobState -> this.jobService.saveJobState(jobState, JobState.Type.RUNNING))
        .zipWith(Mono.just(this.getClass())
            .doOnNext(clz -> log.info("[{}] Job Start", clz.getSimpleName()))
            .flatMap(unused -> this.executeInternal())
            .flatMap(this.jobService::saveJobLog)
            .doOnNext(jobLog -> log.info("jobLog: {}", jobLog)))
        .delayUntil(TupleUtils.function((state, log) -> Mono.just(state)
            .doOnNext(jobState -> jobState.setCurrentState(JobState.Type.STANDBY))
            .onErrorContinue((throwable, jobState) -> ((JobState) jobState).setCurrentState(JobState.Type.FAILED))
            .flatMap(this.jobService::saveJobState)))
        .map(TupleUtils.function((jobState, jobLog) -> jobLog.isSuccess()))
        .doOnNext(isSuccess -> log.info("[{}] Job End, success: {}", this.getClass().getSimpleName(), isSuccess))
        .doOnTerminate(observation::stop)
        .doOnError(observation::error)
        .contextWrite(context -> context.put(ObservationThreadLocalAccessor.KEY, observation));
  }

  protected abstract Class<S> getJobStateClz();
  protected abstract S defaultJobState();
  // must not Empty
  protected abstract Mono<L> executeInternal();
}
