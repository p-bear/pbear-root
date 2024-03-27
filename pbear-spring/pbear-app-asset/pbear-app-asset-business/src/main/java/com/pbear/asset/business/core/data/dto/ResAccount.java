package com.pbear.asset.business.core.data.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.pbear.asset.business.core.data.type.InterestType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ResAccount {
  private String id;
  private Long ownerId;
  private String accountNumber;
  private String bankName;
  private LocalDateTime createDate;
  private String alias;
  private Float interestRate;
  private InterestType interestType;
  private LocalDateTime endDate;
}
