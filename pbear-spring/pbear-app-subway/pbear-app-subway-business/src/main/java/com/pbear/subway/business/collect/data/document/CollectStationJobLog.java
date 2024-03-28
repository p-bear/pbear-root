package com.pbear.subway.business.collect.data.document;

import com.pbear.subway.business.core.document.JobLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subway.stations")
@TypeAlias("CollectStationJobLog")
public class CollectStationJobLog extends JobLog {
  private Long collectedStationCount;

  @Override
  public String toString() {
    return "CollectStationJobLog{" +
        "collectedStationCount=" + collectedStationCount +
        "} " + super.toString();
  }
}
