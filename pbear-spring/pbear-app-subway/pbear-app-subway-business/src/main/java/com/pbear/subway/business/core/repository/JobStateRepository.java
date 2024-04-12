package com.pbear.subway.business.core.repository;

import com.pbear.subway.business.core.document.JobState;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JobStateRepository extends ReactiveCrudRepository<JobState, String> {
}
