package com.pbear.subway.business.temp.collect.data.document;

import com.pbear.subway.business.temp.core.document.JobLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "subway.stations")
@TypeAlias("CollectStationStatisticsJobLog")
public class CollectStationStatisticsJobLog extends JobLog {
  private List<String> collectedStationstatisticsDate;
  private String errorMessage;

  public CollectStationStatisticsJobLog(final boolean isSuccess, final List<String> collectedStationstatisticsDate) {
    super(isSuccess);
    this.collectedStationstatisticsDate = collectedStationstatisticsDate;
  }

  public CollectStationStatisticsJobLog(final String errorMessage) {
    this.errorMessage = errorMessage;
  }
}
