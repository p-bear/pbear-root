package com.pbear.subway.business.core.service;

import com.pbear.lib.common.FieldNotValidException;
import com.pbear.lib.common.FieldValidatable;
import com.pbear.subway.business.core.document.Station;
import com.pbear.subway.business.core.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StationService {
  private final StationRepository stationRepository;

  public Mono<Station> saveStation(final Station station) {
    return Mono.just(station)
        .filter(FieldValidatable::isValid)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new FieldNotValidException(Station.class))))
        .flatMap(this.stationRepository::save);
  }
}
