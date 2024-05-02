package com.pbear.subway.business.temp.core.service;

import com.pbear.subway.business.temp.core.document.JobLog;
import com.pbear.subway.business.temp.core.document.JobState;
import com.pbear.subway.business.temp.core.repository.JobLogRepository;
import com.pbear.subway.business.temp.core.repository.JobStateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class JobService {
  private final JobLogRepository jobLogRepository;
  private final JobStateRepository jobStateRepository;

  public <T extends JobState> Mono<T> getJobState(final Class<T> targetClz) {
    return this.jobStateRepository.findAll()
        .filter(jobState -> jobState.getClass() == targetClz)
        .next()
        .cast(targetClz);
  }

  public boolean isJobExecutable(final JobState jobState) {
    return switch (jobState.getCurrentState()) {
      case STANDBY -> true;
      default -> false;
    };
  }

  public <T extends JobState> Mono<T> saveJobState(final T jobState) {
    return this.jobStateRepository.save(jobState);
  }

  public <T extends JobState> Mono<T> saveJobState(final T jobState, final JobState.Type currentState) {
    return Mono.just(jobState)
        .doOnNext(it -> it.setCurrentState(currentState))
        .flatMap(this.jobStateRepository::save);
  }

  public <T extends JobLog> Mono<T> saveJobLog(final T jobLog) {
    return this.jobLogRepository.save(jobLog);
  }
}
