package com.pbear.subway.business.temp.collect.job;

import reactor.core.publisher.Mono;

@FunctionalInterface
public interface AsyncJob<R> {
  Mono<R> execute();
}
