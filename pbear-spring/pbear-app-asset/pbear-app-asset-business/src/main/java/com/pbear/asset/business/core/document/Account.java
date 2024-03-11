package com.pbear.asset.business.core.document;

import com.pbear.lib.function.FieldValidator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@Document(collation = "Account")
@TypeAlias("Account")
public class Account implements FieldValidator {
  @Id
  private String id;
  private Long ownerId;
  private String accountNumber;
  private String createCompanyName;
  private LocalDateTime createDate;
  private String alias;

  @Override
  public boolean isValid() {
    return this.ownerId != null && this.accountNumber != null && this.createCompanyName != null;
  }
}
