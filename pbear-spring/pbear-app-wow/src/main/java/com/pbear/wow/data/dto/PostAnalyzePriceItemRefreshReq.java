package com.pbear.wow.data.dto;

import java.util.ArrayList;
import java.util.List;

public record PostAnalyzePriceItemRefreshReq(
    List<Long> targetItemId
) {
  public PostAnalyzePriceItemRefreshReq() {
    this(new ArrayList<>());
  }
}
