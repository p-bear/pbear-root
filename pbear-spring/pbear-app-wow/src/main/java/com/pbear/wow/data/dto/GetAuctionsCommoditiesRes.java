package com.pbear.wow.data.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.pbear.starter.webflux.JsonObjectToStringDeserializer;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAuctionsCommoditiesRes {
  @JsonProperty("auctions")
  @JsonDeserialize(using = JsonObjectToStringDeserializer.class)
  private String auctionsJson;
}
