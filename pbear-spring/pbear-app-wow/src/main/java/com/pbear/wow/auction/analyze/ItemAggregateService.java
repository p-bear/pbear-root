package com.pbear.wow.auction.analyze;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.wow.auction.CommoditiesDocument;
import com.pbear.wow.auction.CommoditiesRepository;
import com.pbear.wow.data.Auction;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
  public Flux<ItemPriceHistoryDocument> refreshItemPriceHistroyAll() {
    return this.getAllTargetItemDocument()
        .map(TargetItemDocument::getId)
        .collectList()
        .zipWith(this.commoditiesRepository.findAll()
            .distinct(CommoditiesDocument::getCollectedDateTime)
            .collectList())
        .map(TupleUtils.function((targetItemIdList, commoditiesDocumentList) -> {
          Map<Long, ItemPriceHistoryDocument> result = new HashMap<>();
          for (CommoditiesDocument commoditiesDocument : commoditiesDocumentList) {
            String dateTime = commoditiesDocument.getCollectedDateTime();
            Map<Long, List<Auction>> groupedAuction = commoditiesDocument.getAuctions(this.objectMapper).stream()
                .collect(Collectors.groupingBy(auction -> auction.getItem().getId()));
            for (Long targetItemId : targetItemIdList) {
              List<Auction> targetAuctionList = groupedAuction.get(targetItemId);
              if (result.get(targetItemId) == null) {
                result.put(targetItemId, new ItemPriceHistoryDocument(targetItemId, new LinkedMultiValueMap<>()));
              }
              result.get(targetItemId).addAuctionAll(dateTime, targetAuctionList);
            }
          }
          return result.values();
        }))
        .doOnNext(doc -> log.info("save ItemPriceHistoryDocument >> item: {} count: {}",
            doc.stream().map(ItemPriceHistoryDocument::getId).collect(Collectors.toList()),
            doc.size()))
        .flux()
        .flatMap(this.itemPriceHistoryRepository::saveAll);
  }

  public Flux<?> addItemPriceHistroy(final CommoditiesDocument commoditiesDocument) {
    Map<Long, List<Auction>> groupedAuction = commoditiesDocument.getAuctions(this.objectMapper).stream()
        .collect(Collectors.groupingBy(auction -> auction.getItem().getId()));
    return this.targetItemRepository.findAll()
        .map(TargetItemDocument::getId)
        .flatMap(targetItemId -> itemPriceHistoryRepository.findById(targetItemId)
            .defaultIfEmpty(new ItemPriceHistoryDocument(targetItemId, new LinkedMultiValueMap<>())))
        .doOnNext(itemPriceHistoryDocument -> itemPriceHistoryDocument.addAuctionAll(
            commoditiesDocument.getCollectedDateTime(),
            groupedAuction.get(itemPriceHistoryDocument.getId())))
        .flatMap(this.itemPriceHistoryRepository::save);
  }

  public Mono<ItemPriceHistoryDocument> getItemPriceHistoryById(final Long itemId) {
    return this.itemPriceHistoryRepository.findById(itemId);
  }
}
