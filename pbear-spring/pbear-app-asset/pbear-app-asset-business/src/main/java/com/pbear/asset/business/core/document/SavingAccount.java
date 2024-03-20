package com.pbear.asset.business.core.document;

import com.pbear.asset.business.core.data.type.InterestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * 적금형 계좌
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "account")
@TypeAlias("SavingAccount")
public class SavingAccount extends Account {
  private String interestRate;
  private InterestType interestType;
  private LocalDateTime endDate;

  @Override
  public boolean isValid() {
    return super.isValid() && this.interestRate != null && this.interestType != null && endDate != null;
  }
}
