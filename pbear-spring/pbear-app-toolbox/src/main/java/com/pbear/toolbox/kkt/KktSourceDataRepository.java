package com.pbear.toolbox.kkt;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface KktSourceDataRepository extends ReactiveCrudRepository<KktSourceData, String> {
  Flux<KktSourceData> findByName(final String name);
}
