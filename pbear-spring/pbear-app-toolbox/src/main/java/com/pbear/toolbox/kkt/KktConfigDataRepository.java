package com.pbear.toolbox.kkt;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface KktConfigDataRepository extends ReactiveCrudRepository<KktConfigData, String> {
  Flux<KktConfigData> findByName(final String name);
}
