package com.pbear.starter.webflux;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import io.netty.resolver.DefaultAddressResolverGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;

@Configuration
@Slf4j
public class WebClientConfig {
  private static final ObjectMapper OBJECT_MAPPER;

  static {
    OBJECT_MAPPER = new ObjectMapper();
    OBJECT_MAPPER
        .setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE)
        .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
  }

  @Bean
  public WebClient webClient(final WebClient.Builder builder) {
    return builder
        .clientConnector(new ReactorClientHttpConnector(HttpClient.create()
            // DNS Resolver 설정
            .resolver(DefaultAddressResolverGroup.INSTANCE) // Sets the JVM built-in resolver.
            // Connection Timeout 설정
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 1000)
            .doOnConnected(connection ->
                connection
                    // ReadTimeout 설정
                    .addHandlerLast(new ReadTimeoutHandler(3))
                    // WriteTimeout 설정
                    .addHandlerLast(new WriteTimeoutHandler(3)))))
        .exchangeStrategies(ExchangeStrategies.builder()
            .codecs(configurer -> {
              // Date Size 무제한 설정
              configurer.defaultCodecs().maxInMemorySize(-1);
              // 살세 로깅 enable
              configurer.defaultCodecs().enableLoggingRequestDetails(true);
            })
            .build())
        // Req, Res 로깅 필터 등록 설정
        .filter(ExchangeFilterFunction.ofRequestProcessor(WebClientConfig::loggingRequestInfo))
        .filter(ExchangeFilterFunction.ofResponseProcessor(WebClientConfig::loggingResponseInfo))
        .build();
  }

  private static  Mono<ClientRequest> loggingRequestInfo(final ClientRequest request) {
    String requestInfo = null;
    try {
      requestInfo = "Req 3rd > " + request.method() +
          ", URI=" + request.url() +
          ", Header=" + (request.headers().isEmpty() ? "" : OBJECT_MAPPER.writeValueAsString(request.headers())) +
          ", Body=" + OBJECT_MAPPER.writeValueAsString(request.body());
    } catch (JsonProcessingException e) {
      log.error("stackTrace:", e);
    }

    log.info(requestInfo);

    return Mono.just(request);
  }

  private static Mono<ClientResponse> loggingResponseInfo(final ClientResponse response) {
    log.info("Res 3rd > {}", response.statusCode());
    return Mono.just(response);
  }
}
