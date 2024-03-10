package com.pbear.starter.webflux;

import com.pbear.starter.webflux.data.dto.PassportHeaderName;
import com.pbear.starter.webflux.data.dto.PassportInfo;
import com.pbear.starter.webflux.data.exception.PassportNotExistException;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;

import java.util.Objects;

@Component
public class PassportExtractor {
  public PassportInfo getPassportInfo(final ServerRequest serverRequest) {
    if (!serverRequest.headers().asHttpHeaders().containsKey(PassportHeaderName.ID.getHeaderName())
        || !serverRequest.headers().asHttpHeaders().containsKey(PassportHeaderName.MAIN_ID.getHeaderName())) {
      throw new PassportNotExistException();
    }
    return new PassportInfo(
        Long.parseLong(Objects.requireNonNull(serverRequest.headers().firstHeader(PassportHeaderName.ID.getHeaderName()))),
        serverRequest.headers().firstHeader(PassportHeaderName.MAIN_ID.getHeaderName())
    );
  }
}
