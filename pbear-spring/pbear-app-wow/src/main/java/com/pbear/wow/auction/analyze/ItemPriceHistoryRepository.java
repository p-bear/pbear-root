package com.pbear.wow.auction.analyze;

import org.bson.types.ObjectId;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

public interface ItemPriceHistoryRepository extends ReactiveCrudRepository<ItemPriceHistoryDocument, ObjectId> {
  Flux<ItemPriceHistoryDocument> findAllByMetadata_ItemId(final Long itemId);
}
