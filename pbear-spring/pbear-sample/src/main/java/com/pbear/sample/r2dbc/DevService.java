package com.pbear.sample.r2dbc;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class DevService {
  private final DevRepository devRepository;

  public Mono<Dev> getDev(final Long id) {
    return this.devRepository.findById(id)
        .defaultIfEmpty(new Dev())
        .doOnNext(dev -> log.info("get Dev: {}", dev));
  }
}
