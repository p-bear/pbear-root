package com.pbear.subway.business.core.document;

import com.pbear.lib.common.FieldValidatable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "jobs.logs")
@TypeAlias("JobLog")
public class JobLog implements FieldValidatable {
  @Id
  private String id = this.generateId();
  private LocalDateTime createDate = LocalDateTime.now();

  @Override
  public boolean isValid() {
    return !this.hasNullField(this.id, this.createDate);
  }

  protected String generateId() {
    return this.getClass().getSimpleName();
  }
}
