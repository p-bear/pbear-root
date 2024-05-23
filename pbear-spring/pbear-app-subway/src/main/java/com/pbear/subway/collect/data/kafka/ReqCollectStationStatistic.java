package com.pbear.subway.collect.data.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqCollectStationStatistic {
  private String targetDate;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean isForce;
}
