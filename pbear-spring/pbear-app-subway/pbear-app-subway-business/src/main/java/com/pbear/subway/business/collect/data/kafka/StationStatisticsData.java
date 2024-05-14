package com.pbear.subway.business.collect.data.kafka;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class StationStatisticsData {
  private boolean isMatched;
  private String stationId;
  private String lineNum;
  private String stationName;
  private String latitude; // 위도
  private String longitude; // 경도
  private Integer ridePasgrNum;
  private Integer alignPasgrNum;
  private String useDate; // yyyyMMdd
  private String workDate; // yyyyMMdd
}
