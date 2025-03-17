package com.pbear.toolbox.kkt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.pbear.starter.webflux.data.exception.RestException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.function.TupleUtils;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class KktHandler {
  private static final String DATE_PATTERN = "\\d{4}년 \\d{1,2}월 \\d{1,2}일 [ㄱ-ㅣ가-힣]{2} \\d{1,2}:\\d{1,2}";

  private final KktSourceDataRepository kktSourceDataRepository;
  private final KktConfigDataRepository kktConfigDataRepository;
  private final ObjectMapper objectMapper;

  public Mono<ServerResponse> handlePostKkt(final ServerRequest serverRequest) {
    return serverRequest.multipartData()
        .flatMap(this::extractKktContent)
        .map(content -> KktSourceData.builder()
            .name(
                serverRequest.queryParam("filename").orElse("kkt")
                    + "_"
                    + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()))
            .content(content)
            .build())
        .flatMap(this.kktSourceDataRepository::save)
        .flatMap(result -> ServerResponse.ok().bodyValue(Map.of("id", result.getId())));
  }

  public Mono<ServerResponse> handleDeleteKkt(final ServerRequest serverRequest) {
    return Mono.just(serverRequest.pathVariable("name"))
        .flatMap(this.kktSourceDataRepository::deleteAllByName)
        .then(ServerResponse.ok().build());
  }

  public Mono<ServerResponse> handleGetKkt(final ServerRequest serverRequest) {
    return this.kktSourceDataRepository.findAll()
        .map(kktSourceData -> Map.of(
            "name", kktSourceData.getName(),
            "timestamp", kktSourceData.getId().getTimestamp()))
        .collectList()
        .flatMap(dtoList -> ServerResponse.ok()
            .bodyValue(Map.of("kktList", dtoList)));
  }

  public Mono<ServerResponse> handleGetKktCsvWithName(final ServerRequest serverRequest) {
    return Mono.just(serverRequest.pathVariable("name"))
        .flatMapMany(this.kktSourceDataRepository::findByName)
        .next()
        .map(KktSourceData::getContent)
        .map(this::toCsvKkt)
        .flatMap(content -> ServerResponse.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"kkt_"
                + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date())
                + ".csv\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .bodyValue(content))
        .switchIfEmpty(ServerResponse.status(HttpStatus.NO_CONTENT).build());
  }

  public Mono<ServerResponse> handleGetKktJsonWithName(final ServerRequest serverRequest) {
    return Mono.just(serverRequest.pathVariable("name"))
        .flatMapMany(this.kktSourceDataRepository::findByName)
        .next()
        .map(KktSourceData::getContent)
        .flatMap(content -> {
          try {
            return Mono.just(this.objectMapper.writeValueAsString(this.toKktDataStream(content).toList()));
          } catch (JsonProcessingException e) {
            return Mono.error(new RestException(HttpStatusCode.valueOf(500), "500"));
          }
        })
        .flatMap(resBody -> ServerResponse.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"kkt_"
                + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date())
                + ".json\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .bodyValue(resBody))
        .switchIfEmpty(ServerResponse.status(HttpStatus.NO_CONTENT).build());
  }

  public Mono<ServerResponse> handlePostMini(final ServerRequest serverRequest) {

    return serverRequest.multipartData()
        .flatMap(this::extractKktContent)
        .map(this::minifyKkt)
        .flatMap(content -> ServerResponse.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"kkt_" + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date()) + ".txt\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .bodyValue(content));
  }

  public Mono<ServerResponse> handlePostCsv(final ServerRequest serverRequest) {
    return serverRequest.multipartData()
        .flatMap(this::extractKktContent)
        .map(this::toCsvKkt)
        .flatMap(content -> ServerResponse.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"kkt_"
                + new SimpleDateFormat("yyyyMMdd_HHmm").format(new Date())
                + "."
                + serverRequest.queryParam("ext").orElse("csv")
                + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .bodyValue(content));
  }

  public Mono<ServerResponse> handlePostKktConfig(final ServerRequest serverRequest) {
    return serverRequest.formData()
        .zipWhen(formData -> {
          final String name = Optional.ofNullable(formData.getFirst("configName")).orElse("only_one");
          return this.kktConfigDataRepository.findByName(name)
              .next()
              .defaultIfEmpty(KktConfigData.builder().name(name).build());
        })
        .map(TupleUtils.function((formData, kktConfigData) -> {
          kktConfigData.setPrefix(formData.getFirst("prefix"));
          kktConfigData.setSuffix(formData.getFirst("suffix"));
          return kktConfigData;
        }))
        .flatMap(this.kktConfigDataRepository::save)
        .flatMap(kktConfigData -> ServerResponse.ok().bodyValue(kktConfigData));
  }

  public Mono<ServerResponse> handleGetKktConfig(final ServerRequest serverRequest) {
    final String name = serverRequest.queryParam("configName").orElse("only_one");
    return this.kktConfigDataRepository.findByName(name)
        .next()
        .defaultIfEmpty(KktConfigData.builder().name(name).prefix("").suffix("").build())
        .flatMap(kktConfigData -> ServerResponse.ok().bodyValue(kktConfigData));
  }

  private Mono<String> extractKktContent(final MultiValueMap<String, Part> multipartForm) {
    if (multipartForm.get("txt") != null) {
      return Flux.fromIterable(multipartForm.get("txt"))
          .cast(FormFieldPart.class)
          .next()
          .map(FormFieldPart::value);
    }
    if (multipartForm.get("file") != null) {
      return Flux.fromIterable(multipartForm.get("file"))
          .cast(FilePart.class)
          .next()
          .flatMap(filePart -> filePart.content()
              .map(dataBuffer -> {
                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                dataBuffer.read(bytes);
                DataBufferUtils.release(dataBuffer);
                return new String(bytes);
              })
              .collect(Collectors.joining()));
    }
    return Mono.empty();
  }

  private String minifyKkt(final String content) {
    final StringBuilder builder = new StringBuilder();

    boolean startFlag = true;
    for (String line : content.replaceAll("\\r", "").split("\\n")) {
      if (startFlag) {
        if (!line.contains(",") || !line.contains(" : ")) {
          continue;
        }
        startFlag = false;
      }

      if (line.trim().isEmpty()) {
        continue;
      }

      if (Pattern.matches(DATE_PATTERN, line.trim())) {
        continue;
      }

      if (!line.contains(",") || !line.contains(" : ")) {
        builder.append(" ");
      } else {
        builder.append(System.lineSeparator());
      }

      builder.append(line);
    }

    return builder.toString();
  }

  private String toCsvKkt(final String content) {
    return "\uFEFF" // for UTF-8 BOM
        + "t,p,m"
        + System.lineSeparator()
        + this.toKktDataStream(content)
        .map(KktData::toString)
        .collect(Collectors.joining(System.lineSeparator()));
  }

  private Stream<KktData> toKktDataStream(final String content) {
    return Arrays.stream(content.replaceAll("\\r", "").split("\\n"))
        .map(String::trim)
        .reduce((s1, s2) -> s1 + (s2.startsWith("202") ? "\n" : " ") + s2)
        .stream()
        .flatMap(formattedContent -> Arrays.stream(formattedContent.split("\\n")))
        .map(String::trim)
        .filter(line -> !line.isEmpty())
        .map(this::toKktData)
        .filter(Objects::nonNull);
  }

  private KktData toKktData(final String line) {
    if (!line.contains(",")) {
      return null;
    }

    String[] parts = line.split(",");

    ZonedDateTime time;
    try {
      time = ZonedDateTime.parse(
          parts[0],
          DateTimeFormatter
              .ofPattern("yyyy년 M월 d일 a h:mm")
              .withLocale(Locale.KOREAN)
              .withZone(ZoneId.systemDefault())
      );
    } catch (Exception e) {
      return null;
    }

    String secondPart = Arrays.stream(parts)
        .skip(1)
        .collect(Collectors.joining(" "));
    if (!secondPart.contains(":")) {
      return null;
    }

    String[] splitedSecondPart = secondPart.split(":");

    String speaker = splitedSecondPart[0].trim();

    String message = Arrays.stream(splitedSecondPart)
        .skip(1)
        .map(String::trim)
        .collect(Collectors.joining(" "));

    return new KktData(time, speaker, message);
  }

  private record KktData(
      ZonedDateTime time,
      String speaker,
      String message
  ) {
    @Override
    public String toString() {
      return time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
          + ","
          + speaker
          + ","
          + message;
    }
  }
}
