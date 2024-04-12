package com.pbear.subway.business.collect.data.document;

import com.pbear.subway.business.core.document.JobLog;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "subway.stations")
@TypeAlias("CollectStationStatisticsJobLog")
public class CollectStationStatisticsJobLog extends JobLog {
}
