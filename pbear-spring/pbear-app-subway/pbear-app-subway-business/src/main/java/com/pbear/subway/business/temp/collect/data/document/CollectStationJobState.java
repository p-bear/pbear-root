package com.pbear.subway.business.temp.collect.data.document;

import com.pbear.subway.business.temp.core.document.JobState;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "jobs.states")
@TypeAlias("CollectStationJobState")
public class CollectStationJobState extends JobState {
}
