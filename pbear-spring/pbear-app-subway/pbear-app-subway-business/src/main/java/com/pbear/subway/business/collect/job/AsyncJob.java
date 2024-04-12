package com.pbear.subway.business.collect.job;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface AsyncJob<R> {
  Mono<R> execute();
}
