package com.pbear.subway.business.core.service;

import com.pbear.subway.business.core.document.JobLog;
import com.pbear.subway.business.core.repository.JobLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class JobLogService {
  private final JobLogRepository jobLogRepository;

  public <T extends JobLog> Mono<T> saveJobLog(final T jobLog) {
    return this.jobLogRepository.save(jobLog);
  }
}
