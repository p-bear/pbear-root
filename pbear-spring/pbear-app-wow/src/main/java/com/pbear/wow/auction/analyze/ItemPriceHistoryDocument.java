package com.pbear.wow.auction.analyze;

import com.pbear.wow.data.Auction;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "itemPriceHistory")
@TypeAlias("ItemPriceHistoryDocument")
public class ItemPriceHistoryDocument {
  @Id
  private Long id;
  private LocalDateTime lastModified = LocalDateTime.now();
  private MultiValueMap<String, Auction> auctionMap; // <yyyyMMdd-HHmmss, auction>

  public ItemPriceHistoryDocument(final Long id, final MultiValueMap<String, Auction> auctionMap) {
    this.id = id;
    this.auctionMap = auctionMap;
  }

  public void addAuctionAll(final String dateTime, final List<Auction> auction) {
    if (dateTime == null || auction == null || auction.isEmpty()) {
      return;
    }
    if (auctionMap == null) {
      this.auctionMap = new LinkedMultiValueMap<>();
    }
    this.auctionMap.addAll(dateTime, auction);
    this.lastModified = LocalDateTime.now();
  }
}
