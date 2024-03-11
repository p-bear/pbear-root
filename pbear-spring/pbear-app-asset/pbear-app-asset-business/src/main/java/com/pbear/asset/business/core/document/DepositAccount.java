package com.pbear.asset.business.core.document;

import com.pbear.asset.business.core.data.type.InterestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@NoArgsConstructor
@Document(collation = "Account")
@TypeAlias("DepositAccount")
public class DepositAccount extends Account {
  private Float interestRate;
  private InterestType interestType;

  @Override
  public boolean isValid() {
    return super.isValid() && this.interestRate != null && this.interestType != null;
  }
}
