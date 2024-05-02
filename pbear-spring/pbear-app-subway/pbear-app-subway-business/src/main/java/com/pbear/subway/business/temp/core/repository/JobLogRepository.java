package com.pbear.subway.business.temp.core.repository;

import com.pbear.subway.business.temp.core.document.JobLog;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobLogRepository extends ReactiveCrudRepository<JobLog, String> {
}
