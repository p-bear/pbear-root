package com.pbear.wow.auction.analyze;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.wow.auction.CommoditiesDocument;
import com.pbear.wow.auction.CommoditiesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemAggregateService {
  private final ObjectMapper objectMapper;
  private final TargetItemRepository targetItemRepository;
  private final ItemPriceHistoryRepository itemPriceHistoryRepository;
  private final CommoditiesRepository commoditiesRepository;

  // -------------------- target item --------------------
  public Flux<TargetItemDocument> getAllTargetItemDocument() {
    return this.targetItemRepository.findAll();
  }

  public Mono<TargetItemDocument> saveTargetItemDocument(TargetItemDocument targetItemDocument) {
    return this.targetItemRepository.save(targetItemDocument);
  }

  public Mono<TargetItemDocument> deleteTargetItemDocument(TargetItemDocument targetItemDocument) {
    return Mono.just(targetItemDocument)
        .delayUntil(this.targetItemRepository::delete);
  }


  // -------------------- item price history --------------------
  public Flux<ItemPriceHistoryDocument> refreshItemPriceHistroy() {
    return this.itemPriceHistoryRepository.deleteAll()
        .thenMany(this.addCommoditiesWithAllTarget(this.commoditiesRepository.findAll()));
  }

  public Flux<ItemPriceHistoryDocument> addCommoditiesWithAllTarget(final Flux<CommoditiesDocument> commoditiesDocumentFlux) {
    return this.getAllTargetItemDocument()
        .map(TargetItemDocument::getId)
        .collectList()
        .doOnNext(targetItemIds -> log.info("targetItemIds: {}", targetItemIds))
        .flatMapMany(targetItemIds -> commoditiesDocumentFlux
            .doOnNext(commoditiesDocument -> log.info("handle commodites >> datetime: {}", commoditiesDocument.getCollectedDateTime()))
            .flatMap(commoditiesDocument -> this.addItemPriceHistroy(targetItemIds, commoditiesDocument)));
  }

  public Flux<ItemPriceHistoryDocument> addItemPriceHistroy(final List<Long> targetItemIds,
                                                            final CommoditiesDocument commoditiesDocument) {
    return Flux.just(this.toItemPriceHistoryDocument(commoditiesDocument, targetItemIds))
        .doOnNext(itemPriceHistoryDocuments -> log.info("[addItemPriceHistroy] try save history >> count: {}", itemPriceHistoryDocuments.size()))
        .flatMap(this.itemPriceHistoryRepository::saveAll);
  }

  public Flux<ItemPriceHistoryDocument> getItemPriceHistoryById(final Long itemId) {
    return this.itemPriceHistoryRepository.findAllByMetadata_ItemId(itemId);
  }

  private List<ItemPriceHistoryDocument> toItemPriceHistoryDocument(final CommoditiesDocument commoditiesDocument,
                                                                    final Collection<Long> targetItemIds) {
    final Instant timestamp = LocalDateTime
        .parse(commoditiesDocument.getCollectedDateTime(), DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"))
        .atZone(ZoneId.systemDefault())
        .toInstant();
    return commoditiesDocument
        .getAuctions(this.objectMapper).stream()
        .filter(auction -> targetItemIds.contains(auction.getItem().getId()))
        .map(auction -> new ItemPriceHistoryDocument(
            timestamp,
            auction.getItem().getId(),
            auction.getUnitPrice(),
            auction.getQuantity(),
            auction.getTimeLeft()))
        .toList();
  }
}
