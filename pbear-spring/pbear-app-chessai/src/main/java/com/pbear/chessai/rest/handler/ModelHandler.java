package com.pbear.chessai.rest.handler;

import com.pbear.chessai.core.model.ModelProvider;
import com.pbear.starter.webflux.data.dto.CommonRestResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class ModelHandler {
  private final ModelProvider modelProvider;

  public Mono<ServerResponse> postModel(final ServerRequest request) {
    final String modelName = request.pathVariable("modelName");

    return request.multipartData()
        .flatMap(data -> {
          final FilePart filePart = (FilePart) data.getFirst("file");
          assert filePart != null;
          return DataBufferUtils.join(filePart.content())
              .flatMap(dataBuffer -> this.modelProvider.saveModel(modelName, dataBuffer.asInputStream()));
        })
        .flatMap(model -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .build()));
  }

  public Mono<ServerResponse> getModel(final ServerRequest request) {
    final String modelName = request.pathVariable("modelName");
    return this.modelProvider.getModel(modelName)
        .flatMap(model -> ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of(
                "exist", true,
                "epochCount", model.data().getEpochCount()
            ))
            .build()))
        .switchIfEmpty(ServerResponse.ok().bodyValue(CommonRestResponse.builder()
            .data(Map.of("exist", false))
            .build()));
  }
}
