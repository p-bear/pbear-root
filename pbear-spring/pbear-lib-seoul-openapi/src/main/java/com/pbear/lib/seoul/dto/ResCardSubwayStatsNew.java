package com.pbear.lib.seoul.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pbear.lib.common.FieldValidatable;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class ResCardSubwayStatsNew implements FieldValidatable {
  @JsonProperty("CardSubwayStatsNew")
  private CardSubwayStatsNew cardSubwayStatsNew;

  @Override
  public boolean isValid() {
    return this.cardSubwayStatsNew.isValid();
  }

  @Getter
  @Setter
  @ToString
  public static class CardSubwayStatsNew implements FieldValidatable {
    @JsonProperty("list_total_count")
    private Long listTotalCount;
    @JsonProperty("RESULT")
    private ResSubwayStationMaster.Result result;
    private List<SubwayStats> row;

    @Override
    public boolean isValid() {
      return this.listTotalCount != null && this.result.isValid();
    }
  }

  @Getter
  @Setter
  public static class Result implements FieldValidatable {
    @JsonProperty("CODE")
    private String code;
    @JsonProperty("MESSAGE")
    private String message;

    @Override
    public boolean isValid() {
      return !hasNullField(this.code);
    }
  }

  @Getter
  @Setter
  @ToString
  public static class SubwayStats implements FieldValidatable {
    @JsonProperty("USE_DT")
    private String useDt;
    @JsonProperty("LINE_NUM")
    private String lineNum;
    @JsonProperty("SUB_STA_NM")
    private String subStaNm;
    @JsonProperty("RIDE_PASGR_NUM")
    private Integer ridePasgrNum;
    @JsonProperty("ALIGHT_PASGR_NUM")
    private Integer alignPasgrNum;
    @JsonProperty("WORK_DT")
    private String workDt;


    @Override
    public boolean isValid() {
      return !this.hasNullField(this.useDt, this.subStaNm, this.ridePasgrNum, this.alignPasgrNum);
    }
  }
}
