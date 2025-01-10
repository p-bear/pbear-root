package com.pbear.toolbox.kkt;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
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

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class KktHandler {
  private static final String DATE_PATTERN = "\\d{4}년 \\d{1,2}월 \\d{1,2}일 [ㄱ-ㅣ가-힣]{2} \\d{1,2}:\\d{1,2}";

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
                + serverRequest.queryParam("ext").orElse("txt")
                + "\"")
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .bodyValue(content));
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
    return Arrays.stream(content.replaceAll("\\r", "").split("\\n"))
        .map(String::trim)
        .reduce((s1, s2) -> s1 + (s2.startsWith("202") ? "\n" : " ") + s2)
        .stream()
        .flatMap(formattedContent -> Arrays.stream(formattedContent.split("\\n")))
        .map(String::trim)
        .filter(line -> !line.isEmpty())
        .map(this::toKktData)
        .filter(Objects::nonNull)
        .map(KktData::toString)
        .collect(Collectors.joining(System.lineSeparator()));
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
