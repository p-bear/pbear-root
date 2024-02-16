package com.pbear.user.core.service;

import com.pbear.user.core.data.entity.UserInfo;
import com.pbear.user.core.data.exception.UserNotFoundException;
import com.pbear.user.core.repository.UserInfoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserMainService {
  private final UserInfoRepository userInfoRepository;
  private final PasswordEncoder passwordEncoder;

  public Mono<UserInfo> getUserInfo(final Long id) {
    return this.userInfoRepository.findById(id)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new UserNotFoundException("id=" + id))));
  }

  public Mono<UserInfo> getUserInfo(final String mainId) {
    return this.userInfoRepository.findUserInfoByMainId(mainId)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new UserNotFoundException("mainId=" + mainId))));
  }

  // TODO: kafka event
  public Mono<UserInfo> createUserInfo(final String mainId, final String password) {
     return Mono.just(password)
         .map(this.passwordEncoder::encode)
         .map(encodedPassword -> new UserInfo(mainId, encodedPassword))
         .flatMap(this.userInfoRepository::save)
         .doOnNext(userInfo -> log.info("user created, id: {}, mainId: {}", userInfo.getId(), userInfo.getMainId()));
  }

  public Mono<Boolean> isPasswordMatches(final String mainId, final String rawPassword) {
    return this.userInfoRepository.findUserInfoByMainId(mainId)
        .map(userInfo -> this.passwordEncoder.matches(rawPassword, userInfo.getPassword()));
  }
}
