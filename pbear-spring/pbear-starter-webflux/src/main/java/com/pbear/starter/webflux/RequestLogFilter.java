package com.pbear.starter.webflux;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.channels.Channels;
import java.util.Optional;

@Component
@Slf4j
public class RequestLogFilter implements WebFilter {
  @Override
  public Mono<Void> filter(final ServerWebExchange exchange, final WebFilterChain chain) {
    return chain.filter(new LoggingWebExchange(exchange));
  }

  public static class LoggingWebExchange extends ServerWebExchangeDecorator {
    private final LoggingRequestDecorator loggingRequestDecorator;

    protected LoggingWebExchange(final ServerWebExchange delegate) {
      super(delegate);
      this.loggingRequestDecorator = new LoggingRequestDecorator(delegate.getRequest());
    }

    @Override
    public ServerHttpRequest getRequest() {
      return this.loggingRequestDecorator;
    }
  }

  public static class LoggingRequestDecorator extends ServerHttpRequestDecorator {
    private Flux<DataBuffer> body;

    public LoggingRequestDecorator(final ServerHttpRequest delegate) {
      super(delegate);
      this.init();
    }

    @Override
    public Flux<DataBuffer> getBody() {
      return this.body;
    }

    @SuppressWarnings("deprecation")
    private void init() {
      String path = super.getDelegate().getURI().getPath();
      String query = super.getDelegate().getURI().getQuery();
      String method = Optional.of(super.getDelegate().getMethod()).orElse(HttpMethod.GET).name();
      String headers = super.getDelegate().getHeaders().toString();
      log.info("Request: {} {}, headers: {}", method, path + (query != null ? "?" + query : ""), headers);
      this.body = super.getBody()
          .publishOn(Schedulers.boundedElastic())
          .doOnNext(dataBuffer -> {
            ByteArrayOutputStream bodyStream = new ByteArrayOutputStream();
            try {
              Channels.newChannel(bodyStream).write(dataBuffer.asByteBuffer().asReadOnlyBuffer());
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
            log.info("body: {}", bodyStream);
          });
    }
  }
}
