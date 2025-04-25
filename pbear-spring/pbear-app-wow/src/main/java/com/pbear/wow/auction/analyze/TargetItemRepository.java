package com.pbear.wow.auction.analyze;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface TargetItemRepository extends ReactiveCrudRepository<TargetItemDocument, Long> {
}
