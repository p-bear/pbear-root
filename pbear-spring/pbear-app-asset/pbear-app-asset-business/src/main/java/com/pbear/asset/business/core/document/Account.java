package com.pbear.asset.business.core.document;

import com.pbear.lib.function.FieldValidator;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.TypeAlias;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@Document(collection = "account")
@TypeAlias("Account")
public class Account implements FieldValidator {
  @Id
  private String id;
  private Long ownerId;
  private String accountNumber;
  private String bankName;
  private LocalDateTime createDate;
  private String alias;
  private Set<String> tags;

  @Override
  public boolean isValid() {
    return this.ownerId != null && this.accountNumber != null && this.bankName != null;
  }
}
