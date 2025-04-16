package com.pbear.wow.auction;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface CommoditiesRepository extends ReactiveCrudRepository<CommoditiesDocument, String> {
  Flux<CommoditiesDocument> findAllByCollectedDate(final String collectedDate);
}
