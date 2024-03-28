package com.pbear.starter.webflux.util;

import com.pbear.lib.common.FieldNotValidException;
import com.pbear.lib.common.FieldValidatable;
import reactor.core.publisher.Mono;

public interface FieldValidator {
  static <T extends FieldValidatable> Mono<Boolean> validate(final T target) {
    return Mono.just(target)
        .filter(FieldValidatable::isValid)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new FieldNotValidException(target.getClass()))))
        .then(Mono.just(true));
  }
}
