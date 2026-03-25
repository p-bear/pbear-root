package com.pbear.chessai.core.model;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.util.ModelSerializer;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsResource;
import org.springframework.data.mongodb.gridfs.ReactiveGridFsTemplate;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class ModelProvider {
  private static final String DEFAULT_MODEL_NAME = "pbear";
  private final Map<String, Mono<Model>> cachedModels = new ConcurrentHashMap<>();

  private final ReactiveGridFsTemplate gridFsTemplate;

  @PostConstruct
  public void init() {
    // preload default
    this.getModel(DEFAULT_MODEL_NAME)
        .subscribe();
  }

  public Mono<Model> saveModel(final String modelName, final InputStream inputStream) {
    return Mono.just(inputStream)
        .flatMap(is -> {
          try {
            MultiLayerNetwork multiLayerNetwork = ModelSerializer.restoreMultiLayerNetwork(is);
            return this.saveModel(modelName, multiLayerNetwork);
          } catch (IOException e) {
            return Mono.error(e);
          }
        })
        .subscribeOn(Schedulers.boundedElastic());
  }

  public Mono<Model> saveModel(final String modelName, final MultiLayerNetwork modelData) {
    final Mono<Model> modelMono = Mono.just(new Model(modelName, modelData, new Date())).cache();
    this.cachedModels.put(modelName, modelMono);

    return modelMono
        .delayUntil(model -> {
          Flux<DataBuffer> content = this.toDataBufferFlux(outputStream -> {
            try {
              ModelSerializer.writeModel(model.data(), outputStream, true);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          });

          return this.gridFsTemplate
              .store(content, model.name(), new Document().append("updateDate", model.updateDate()));
        });
  }

  public Mono<Model> getModel(final String name) {
    final String modelName = name == null || name.isEmpty() ? DEFAULT_MODEL_NAME : name;

    if (this.cachedModels.containsKey(modelName)) {
      return this.cachedModels.get(modelName);
    }

    Mono<Model> modelMono = this.gridFsTemplate
        .findFirst(Query.query(GridFsCriteria.where("filename").is(modelName)))
        .flatMap(gridFSFile -> gridFsTemplate.getResource(gridFSFile)
            .flatMap(ReactiveGridFsResource::getInputStream)
            .flatMap(inputStream -> {
              final Document metadata = gridFSFile.getMetadata();
              final Date updateDate = metadata != null ?
                  gridFSFile.getMetadata().get("updateDate", Date.class)
                  : new Date();
              try {
                return Mono.just(new Model(gridFSFile.getFilename(),
                    ModelSerializer.restoreMultiLayerNetwork(inputStream), updateDate))
                    .subscribeOn(Schedulers.boundedElastic());
              } catch (IOException e) {
                return Mono.error(e);
              }
            })
        ).cache();
    this.cachedModels.put(modelName, modelMono);
    return modelMono;
  }

  private Flux<DataBuffer> toDataBufferFlux(Consumer<OutputStream> writer) {
    DataBufferFactory factory = new DefaultDataBufferFactory();

    return Flux.create(sink -> {
      PipedInputStream pipedInput = new PipedInputStream(4096);

      try {
        PipedOutputStream pipedOutput = new PipedOutputStream(pipedInput);

        // OutputStream에 write하는 작업을 별도 스레드에서 실행
        Schedulers.boundedElastic().schedule(() -> {
          try (pipedOutput) {
            writer.accept(pipedOutput);
          } catch (IOException e) {
            sink.error(e);
          }
        });

        // PipedInputStream을 읽어서 Flux로 변환
        DataBufferUtils.readInputStream(() -> pipedInput, factory, 4096)
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe(sink::next, sink::error, sink::complete);
      } catch (IOException e) {
        sink.error(e);
      }
    });
  }
}
