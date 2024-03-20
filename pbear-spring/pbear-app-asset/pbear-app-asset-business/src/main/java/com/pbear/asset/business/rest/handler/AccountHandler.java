package com.pbear.asset.business.rest.handler;

import com.pbear.asset.business.core.data.mapper.AccountMapper;
import com.pbear.asset.business.core.service.AccountService;
import com.pbear.starter.webflux.PassportExtractor;
import com.pbear.starter.webflux.data.dto.CommonRestResponse;
import com.pbear.starter.webflux.data.dto.PassportInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class AccountHandler {
  private final AccountService accountService;

  public Mono<ServerResponse> handleGetAccounts(final ServerRequest serverRequest) {
    PassportInfo passportInfo = PassportExtractor.getPassportInfo(serverRequest);
    return this.accountService.getAccountByOwnerId(passportInfo.id())
        .collectMultimap(
            account -> account.getClass().getSimpleName(),
            AccountMapper.INSTANCE::toResAccountByInstance
        )
        .flatMap(resAccountList -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(resAccountList)
            .build()));
  }
}
