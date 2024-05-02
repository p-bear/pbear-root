package com.pbear.subway.business.temp.core.document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "jobs.states")
@TypeAlias("JobState")
@ToString
public class JobState {
  @Id
  private String id;
  private Type currentState = Type.STANDBY;
  private LocalDateTime startTime = LocalDateTime.now();
  private LocalDateTime endTime;

  public enum Type {
    STANDBY, RUNNING, FAILED
  }
}
