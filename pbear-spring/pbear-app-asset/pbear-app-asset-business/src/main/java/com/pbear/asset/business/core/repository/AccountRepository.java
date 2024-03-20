package com.pbear.asset.business.core.repository;

import com.pbear.asset.business.core.document.Account;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AccountRepository extends ReactiveCrudRepository<Account, String> {
  Flux<Account> findAllByOwnerId(final Long ownerId);
}
