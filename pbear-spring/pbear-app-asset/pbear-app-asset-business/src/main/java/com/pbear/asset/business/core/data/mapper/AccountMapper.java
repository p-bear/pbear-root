package com.pbear.asset.business.core.data.mapper;

import com.pbear.asset.business.core.data.dto.ResAccount;
import com.pbear.asset.business.core.document.Account;
import com.pbear.asset.business.core.document.DepositAccount;
import com.pbear.asset.business.core.document.SavingAccount;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AccountMapper {
  AccountMapper INSTANCE = Mappers.getMapper(AccountMapper.class);

  default ResAccount toResAccountByInstance(Account account) {
    if (account == null) {
      return null;
    }
    if (account instanceof DepositAccount depositAccount) {
      return this.toResAccount(depositAccount);
    } else if (account instanceof SavingAccount savingAccount) {
      return this.toResAccount(savingAccount);
    }

    return this.toResAccount(account);
  }
  ResAccount toResAccount(Account account);
  ResAccount toResAccount(DepositAccount depositAccount);
  ResAccount toResAccount(SavingAccount savingAccount);
}
