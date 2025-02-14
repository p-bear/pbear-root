package com.pbear.toolbox.kkt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "kkt")
@TypeAlias("KktConfigData")
public class KktConfigData {
  @Id
  private String id;
  private String prefix;
  private String suffix;
}
