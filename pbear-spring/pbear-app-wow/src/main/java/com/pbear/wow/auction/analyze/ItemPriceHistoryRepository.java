package com.pbear.wow.auction.analyze;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface ItemPriceHistoryRepository extends ReactiveCrudRepository<ItemPriceHistoryDocument, Long> {
}
