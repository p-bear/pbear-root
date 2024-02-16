package com.pbear.user.rest.handler;

import com.pbear.starter.webflux.data.dto.CommonRestResponse;
import com.pbear.starter.webflux.data.exception.RestException;
import com.pbear.user.core.data.exception.UserNotFoundException;
import com.pbear.user.core.mapper.UserInfoMapper;
import com.pbear.user.core.service.UserMainService;
import com.pbear.user.rest.dto.ReqPostUserInfo;
import com.pbear.user.rest.dto.ReqPostUserMainPassword;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserMainHandler {
  private final UserMainService userMainService;

  public Mono<ServerResponse> handleGetUserMainId(final ServerRequest serverRequest) {
    return Mono.just(serverRequest.pathVariable("id"))
        .map(Long::parseLong)
        .onErrorMap(throwable -> new RestException(HttpStatus.BAD_REQUEST, "E400", "id must be Long"))
        .flatMap(this.userMainService::getUserInfo)
        .onErrorMap(UserNotFoundException.class,
            userNotFoundException -> new RestException(HttpStatus.BAD_REQUEST, "E404", userNotFoundException.getQuery()))
        .map(UserInfoMapper.INSTANCE::toResGetUserInfo)
        .flatMap(resGetUserInfo -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(resGetUserInfo)
            .build()));
  }

  public Mono<ServerResponse> handleGetUserMain(final ServerRequest serverRequest) {
    return Mono.just(serverRequest.queryParam("mainId"))
        .filter(Optional::isPresent)
        .switchIfEmpty(Mono.defer(() -> Mono.error(new RestException(HttpStatus.BAD_REQUEST, "E400", "mainId=null"))))
        .map(Optional::get)
        .flatMap(this.userMainService::getUserInfo)
        .onErrorMap(UserNotFoundException.class,
            userNotFoundException -> new RestException(HttpStatus.BAD_REQUEST, "E404", userNotFoundException.getQuery()))
        .map(UserInfoMapper.INSTANCE::toResGetUserInfo)
        .flatMap(resGetUserInfo -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(resGetUserInfo)
            .build()));
  }

  public Mono<ServerResponse> handlePostUserMain(final ServerRequest serverRequest) {
    return serverRequest.bodyToMono(ReqPostUserInfo.class)
        .flatMap(reqPostUserInfo -> this.userMainService.createUserInfo(reqPostUserInfo.mainId(), reqPostUserInfo.password()))
        .onErrorMap(DuplicateKeyException.class,
            duplicateKeyException -> new RestException("E5000", duplicateKeyException.getMessage()))
        .map(UserInfoMapper.INSTANCE::toResGetUserInfo)
        .flatMap(resUserInfo -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(resUserInfo)
            .build()));
  }

  public Mono<ServerResponse> handlePostUserMainPassword(final ServerRequest serverRequest) {
    return serverRequest.bodyToMono(ReqPostUserMainPassword.class)
        .flatMap(reqPostUserMainPassword ->
            this.userMainService.isPasswordMatches(reqPostUserMainPassword.mainId(), reqPostUserMainPassword.password()))
        .flatMap(isPasswordMatches -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of("isPasswordMatches", isPasswordMatches))
            .build()));
  }
}
