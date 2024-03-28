package com.pbear.subway.business.core.document;

import com.pbear.lib.common.FieldValidatable;
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
@Document(collection = "jobs.logs")
@TypeAlias("JobLog")
@ToString
public class JobLog implements FieldValidatable {
  @Id
  private String id;
  private String name = this.generateName();
  private LocalDateTime createDate = LocalDateTime.now();

  @Override
  public boolean isValid() {
    return !this.hasNullField(this.name, this.createDate);
  }

  protected String generateName() {
    return this.getClass().getSimpleName();
  }
}
