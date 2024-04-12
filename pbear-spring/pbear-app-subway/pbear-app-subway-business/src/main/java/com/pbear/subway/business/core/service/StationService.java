package com.pbear.subway.business.core.service;

import com.pbear.starter.webflux.util.FieldValidator;
import com.pbear.subway.business.core.document.Station;
import com.pbear.subway.business.core.repository.StationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class StationService {
  private final StationRepository stationRepository;

  public Flux<Station> getAllStations() {
    return this.stationRepository.findAll();
  }

  public Mono<Station> saveStation(final Station station) {
    return Mono.just(station)
        .filterWhen(FieldValidator::validate)
        .flatMap(this.stationRepository::save);
  }
}
