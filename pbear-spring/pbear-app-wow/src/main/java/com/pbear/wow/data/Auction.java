package com.pbear.wow.data;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Auction {
  private Long id;
  private Item item;
  private Long quantity;
  @JsonProperty(value = "unit_price")
  private Long unitPrice;
  @JsonProperty(value = "time_left")
  private String timeLeft;

  @Getter
  @Setter
  public static class Item {
    private Long id;
  }
}
