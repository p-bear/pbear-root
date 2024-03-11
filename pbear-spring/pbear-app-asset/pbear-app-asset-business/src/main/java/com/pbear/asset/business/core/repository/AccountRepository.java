package com.pbear.asset.business.core.repository;

import com.pbear.asset.business.core.document.Account;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface AccountRepository extends ReactiveMongoRepository<Account, String> {
  Flux<Account> findAllByOwnerId(final Long ownerId);
}
