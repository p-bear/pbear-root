package com.pbear.toolbox.kkt;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "kktConfig")
@TypeAlias("KktConfigData")
public class KktConfigData {
  @Id
  private ObjectId id;
  private String name;
  private String prefix;
  private String suffix;
}
