package com.pbear.asset.business.core.service;

import com.pbear.asset.business.core.data.exception.AccountNotExistException;
import com.pbear.asset.business.core.data.exception.AccountNotValidException;
import com.pbear.asset.business.core.document.Account;
import com.pbear.asset.business.core.repository.AccountRepository;
import com.pbear.lib.common.FieldValidatable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
  private final AccountRepository accountRepository;

  public <T extends Account> Mono<T> saveAccount(final T account) throws AccountNotValidException {
    return Mono.just(account)
        .filter(FieldValidatable::isValid)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new AccountNotValidException())))
        .flatMap(this.accountRepository::save);
  }

  public <T extends Account> Mono<T> getAccount(final String accountId, Class<T> clz) throws AccountNotExistException {
    if (accountId == null) {
      throw new RuntimeException("accountId cannot be null");
    }
    return this.accountRepository.findById(accountId)
        .cast(clz)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new AccountNotExistException())));
  }

  public Flux<Account> getAccountByOwnerId(final Long ownerId) {
    return this.accountRepository.findAllByOwnerId(ownerId);
  }

  public <T extends Account> Flux<T> getAccountByOwnerId(final Long ownerId, Class<T> clz) {
    return this.accountRepository.findAllByOwnerId(ownerId)
        .filter(account -> account.getClass() == clz)
        .cast(clz);
  }
}
