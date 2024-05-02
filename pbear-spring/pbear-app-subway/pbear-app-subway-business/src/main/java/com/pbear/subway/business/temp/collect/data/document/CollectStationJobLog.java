package com.pbear.subway.business.temp.collect.data.document;

import com.pbear.subway.business.temp.core.document.JobLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "subway.stations")
@TypeAlias("CollectStationJobLog")
public class CollectStationJobLog extends JobLog {
  private Long collectedStationBeforeCount;
  private Long collectedStationAfterCount;
  private String errorMessage;

  public CollectStationJobLog(final boolean isSuccess, final Long collectedStationBeforeCount, final Long collectedStationAfterCount) {
    super(isSuccess);
    this.collectedStationBeforeCount = collectedStationBeforeCount;
    this.collectedStationAfterCount = collectedStationAfterCount;
  }

  public CollectStationJobLog(final boolean isSuccess, final Long collectedStationBeforeCount, final Long collectedStationAfterCount, final String errorMessage) {
    super(isSuccess);
    this.collectedStationBeforeCount = collectedStationBeforeCount;
    this.collectedStationAfterCount = collectedStationAfterCount;
    this.errorMessage = errorMessage;
  }

  @Override
  public String toString() {
    return "CollectStationJobLog{" +
        "collectedStationBeforeCount=" + collectedStationBeforeCount +
        ", collectedStationAfterCount=" + collectedStationAfterCount +
        ", errorMessage='" + errorMessage + '\'' +
        "} " + super.toString();
  }
}
