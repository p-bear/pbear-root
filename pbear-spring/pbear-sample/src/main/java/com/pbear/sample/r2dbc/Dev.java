package com.pbear.sample.r2dbc;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Table("dev")
@NoArgsConstructor
@Getter
@Setter
@ToString
public class Dev {
  @Id
  private Long id;
  private String devString;
  private Long devNumber;
  private Boolean devBoolean;
  @CreatedDate
  private LocalDateTime creDate;
  @LastModifiedDate
  private LocalDateTime modDate;
  private LocalDate testLocalDate;
  private LocalDateTime testLocalDateTime;
}
