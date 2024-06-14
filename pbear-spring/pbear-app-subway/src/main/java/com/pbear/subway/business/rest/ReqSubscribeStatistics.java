package com.pbear.subway.business.rest;

import com.pbear.lib.common.FieldValidatable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReqSubscribeStatistics implements FieldValidatable {
  private String targetDate;
  private String sessionId;

  @Override
  public boolean isValid() {
    return !this.hasNullField(this.targetDate) && !this.hasNullField(this.sessionId);
  }
}
