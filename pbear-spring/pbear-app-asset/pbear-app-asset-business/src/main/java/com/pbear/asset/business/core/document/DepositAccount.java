package com.pbear.asset.business.core.document;

import com.pbear.asset.business.core.data.type.InterestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * 예금형 계좌
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "account")
@TypeAlias("DepositAccount")
public class DepositAccount extends Account {
  private String interestRate;
  private InterestType interestType;

  @Override
  public boolean isValid() {
    return super.isValid() && this.interestRate != null && this.interestType != null;
  }
}
