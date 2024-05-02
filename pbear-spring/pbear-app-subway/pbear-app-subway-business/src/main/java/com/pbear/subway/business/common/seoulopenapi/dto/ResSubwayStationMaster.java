package com.pbear.subway.business.common.seoulopenapi.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.pbear.lib.common.FieldValidatable;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ResSubwayStationMaster implements FieldValidatable {
  private LocalDateTime createDate = LocalDateTime.now();
  private SubwayStationMaster subwayStationMaster;

  @Override
  public boolean isValid() {
    return !this.hasNullField(this.subwayStationMaster);
  }

  @Getter
  @Setter
  public static class SubwayStationMaster implements FieldValidatable {
    @JsonProperty("list_total_count")
    private Long listTotalCount;
    @JsonProperty("RESULT")
    private Result result;
    private List<Station> row;

    @Override
    public boolean isValid() {
      return !this.hasNullField(this.listTotalCount, this.result, this.row);
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
  public static class Station implements FieldValidatable {
    @JsonProperty("STATN_ID")
    private String statnId;
    @JsonProperty("STATN_NM")
    private String statnNm;
    @JsonProperty("ROUTE")
    private String route;
    @JsonProperty("CRDNT_Y")
    private String crdntY;
    @JsonProperty("CRDNT_X")
    private String crdntX;

    @Override
    public boolean isValid() {
      return !hasNullField(this.statnId, this.statnNm, this.route, this.crdntX, this.crdntY);
    }
  }
}
