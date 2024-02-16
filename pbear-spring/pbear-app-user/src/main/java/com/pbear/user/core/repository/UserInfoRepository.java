package com.pbear.user.core.repository;

import com.pbear.user.core.data.entity.UserInfo;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface UserInfoRepository extends ReactiveCrudRepository<UserInfo, Long> {
  Mono<UserInfo> findUserInfoByMainId(String mainId);
}
