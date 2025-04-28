package com.pbear.wow.auction;

import com.pbear.lib.notify.NotifyClient;
import com.pbear.wow.auction.analyze.ItemAggregateService;
import com.pbear.wow.core.WowApiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuctionDataCollector {
  private final WowApiService wowApiService;
  private final CommoditiesRepository commoditiesRepository;
  private final NotifyClient notifyClient;
  private final ItemAggregateService itemAggregateService;

  @Scheduled(cron = "0 5 * * * *")
  public void collectWowAuctionCommoditiesData() {
    Mono.just("")
        .doOnNext(unused -> log.info("collect wow auctionCommoditiesData start"))
        .flatMap(unused -> this.wowApiService.getAuctionsCommodities())
        .map(data -> new CommoditiesDocument(data.getAuctionsJson()))
        .flatMap(this.commoditiesRepository::save)
        .doOnNext(unused -> log.info("collect wow auctionCommoditiesData end"))
        .onErrorResume(throwable -> this.notifyClient.sendMessage("와우 경매장 데이터 수집 실패: " + throwable.getMessage())
            .flatMap(s -> Mono.empty()))
        .delayUntil(this.itemAggregateService::addItemPriceHistroy)
        .subscribe();
  }
}
