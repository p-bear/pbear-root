package com.pbear.subway.business.data.kafka;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.confluent.ksql.api.client.Row;
import lombok.Getter;

@Getter
public class StationStatisticsJoinData {
  @JsonProperty("ID")
  private final String id;
  @JsonProperty("NAME")
  private final String name;
  @JsonProperty("LINE_NUM")
  private final String lineNum;
  @JsonProperty("RIDE_PASGR_NUM")
  private final Long ridePasgrNum;
  @JsonProperty("ALIGHT_PASGR_NUM")
  private final Long alightPasgrNum;
  @JsonProperty("USE_DATE")
  private final String useDate;
  @JsonProperty("LATITUDE")
  private final String latitude;
  @JsonProperty("LONGITUDE")
  private final String longitude;

  public StationStatisticsJoinData(final Row row) {
    this.id = row.getString("ID");
    this.name = row.getString("NAME");
    this.lineNum = row.getString("LINE_NUM");
    this.ridePasgrNum = row.getLong("RIDE_PASGR_NUM");
    this.alightPasgrNum = row.getLong("ALIGHT_PASGR_NUM");
    this.useDate = row.getString("USE_DATE");
    this.latitude = row.getString("LATITUDE");
    this.longitude = row.getString("LONGITUDE");
  }
}
