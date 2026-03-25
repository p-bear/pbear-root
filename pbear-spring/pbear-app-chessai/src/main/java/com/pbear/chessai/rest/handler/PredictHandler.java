package com.pbear.chessai.rest.handler;

import com.pbear.chessai.core.predict.Predicator;
import com.pbear.chessai.rest.dto.PostPredictReq;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class PredictHandler {
  private final Predicator predicator;

  public Mono<ServerResponse> postPredict(final ServerRequest request) {
    return request.bodyToMono(PostPredictReq.class)
        .flatMap(req -> this.predicator.predict(req.fen(), Math.max(req.topN(), 1), req.modelName())
            .collectList())
        .flatMap(resultList -> ServerResponse.ok()
            .bodyValue(Map.of("moves", resultList)));
  }
}
