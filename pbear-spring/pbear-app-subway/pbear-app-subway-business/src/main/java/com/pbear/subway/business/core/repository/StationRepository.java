package com.pbear.subway.business.core.repository;

import com.pbear.subway.business.core.document.Station;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StationRepository extends ReactiveCrudRepository<Station, String> {
}
