package com.pbear.wow.auction.analyze;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "itemPriceHistory")
@TypeAlias("ItemPriceHistoryDocument")
@ToString
public class ItemPriceHistoryDocument {
  @Id
  private ObjectId id;

  private Instant timestamp; // timeField
  private Meta metadata; // metaField

  private Long quantity;
  private Long unitPrice;
  private String timeLeft;

  public ItemPriceHistoryDocument(final Instant timestamp, final Long itemId, final Long unitPrice,
                                  final Long quantity, final String timeLeft) {
    this.timestamp = timestamp;
    this.metadata = new Meta(itemId);
    this.unitPrice = unitPrice;
    this.quantity = quantity;
    this.timeLeft = timeLeft;
  }

  @Getter
  @Setter
  @NoArgsConstructor
  @AllArgsConstructor
  static class Meta {
    private Long itemId;
  }
}
