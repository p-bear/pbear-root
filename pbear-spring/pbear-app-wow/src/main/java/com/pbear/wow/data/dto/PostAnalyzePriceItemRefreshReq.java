package com.pbear.wow.data.dto;

import java.util.ArrayList;
import java.util.List;

public record PostAnalyzePriceItemRefreshReq(
    List<Long> targetItemList
) {
  public PostAnalyzePriceItemRefreshReq() {
    this(new ArrayList<>());
  }
}
