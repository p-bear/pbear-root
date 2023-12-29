package com.pbear.sample.r2dbc;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;

public interface DevRepository extends ReactiveCrudRepository<Dev, Long> {
}
