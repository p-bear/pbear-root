package com.pbear.toolbox.kkt;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KktConfigDataRepository extends ReactiveCrudRepository<KktConfigData, String> {
}
