package com.pbear.user.core.data.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("user_info")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class UserInfo {
  @Id
  private Long id;
  private String mainId;
  private String password;
  @Setter
  @CreatedDate
  private LocalDateTime creDate;
  @Setter
  @LastModifiedDate
  private LocalDateTime modDate;

  public UserInfo(final String mainId, final String password) {
    this.mainId = mainId;
    this.password = password;
  }
}
