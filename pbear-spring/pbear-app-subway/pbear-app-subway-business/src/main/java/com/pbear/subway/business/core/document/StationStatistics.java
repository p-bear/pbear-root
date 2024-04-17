package com.pbear.subway.business.core.document;

import com.pbear.lib.common.FieldValidatable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document(collection = "subway.stations.statistics")
@TypeAlias("StationStatistics")
public class StationStatistics implements FieldValidatable {
  @Id
  private String id;
  private String stationId;
  private String useDate;
  private Integer ridePassengerNum;
  private Integer alignPassengerNum;

  public StationStatistics generateDefaultId() {
    this.id = this.stationId + "-" + this.useDate;
    return this;
  }

  @Override
  public boolean isValid() {
    return !this.hasNullField(this.stationId, this.useDate, this.ridePassengerNum, this.alignPassengerNum);
  }
}
