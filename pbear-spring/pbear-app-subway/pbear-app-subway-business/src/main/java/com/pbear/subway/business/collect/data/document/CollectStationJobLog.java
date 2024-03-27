package com.pbear.subway.business.collect.data.document;

import com.pbear.subway.business.core.document.JobLog;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "subway.stations")
@TypeAlias("CollectStationJobLog")
@ToString
public class CollectStationJobLog extends JobLog {
  private Long collectedStationCount;
}
