package com.pbear.subway.business.collect.data.document;

import com.pbear.subway.business.core.document.JobState;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "jobs.states")
@TypeAlias("CollectStationStatisticsJobState")
public class CollectStationStatisticsJobState extends JobState {
  private List<String> collectedStationStatistics;
  private Map<String, Set<String>> skipCollectMap;

  public void addCollectedStationStatistic(List<String> useDate) {
    if (this.collectedStationStatistics == null) {
      this.collectedStationStatistics = new ArrayList<>();
    }
    this.collectedStationStatistics.addAll(useDate);
  }

  public void addSkipStation(final String useDate, final String stationName, final String stationLine) {
    if (this.skipCollectMap == null) {
      this.skipCollectMap = new HashMap<>();
    }
    if (this.skipCollectMap.containsKey(useDate)) {
      this.skipCollectMap.get(useDate).add(stationName + "," + stationLine);
      return;
    }
    this.skipCollectMap.put(useDate, new HashSet<>(Set.of(stationName)));
  }
}
