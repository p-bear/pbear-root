package com.pbear.subway.business.core.document;

import com.pbear.lib.common.FieldValidatable;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Builder
@Document(collection = "subway.stations")
@TypeAlias("Station")
public class Station implements FieldValidatable {
  @Id
  private String id;
  private String name;
  private String routeName;
  private Double latitude; // 위도,x,동서
  private Double longitude; // 경도,y,남북

  @Override
  public boolean isValid() {
    return !this.hasNullField(this.id, this.name, this.routeName, this.latitude, this.longitude);
  }
}
