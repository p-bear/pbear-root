package com.pbear.starter.webflux;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Configuration
public class JacksonConfig {
  @Bean
  public Jackson2ObjectMapperBuilderCustomizer jackson2ObjectMapperBuilderCustomizer() {
    return builder -> builder
        .serializers(new LocalDateTimeEpochMilliSerializer())
        .serializers(new LocalDateEpochMilliSerializer());
  }

  public static class LocalDateTimeEpochMilliSerializer extends StdSerializer<LocalDateTime> {
    public LocalDateTimeEpochMilliSerializer() {
      super(LocalDateTime.class);
    }

    @Override
    public void serialize(final LocalDateTime localDateTime, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
      if (localDateTime != null) {
        jsonGenerator.writeNumber(localDateTime.toInstant(OffsetDateTime.now().getOffset()).toEpochMilli());
      } else {
        jsonGenerator.writeNull();
      }
    }
  }

  public static class LocalDateEpochMilliSerializer extends StdSerializer<LocalDate> {
    public LocalDateEpochMilliSerializer() {
      super(LocalDate.class);
    }

    @Override
    public void serialize(final LocalDate localDate, final JsonGenerator jsonGenerator, final SerializerProvider serializerProvider) throws IOException {
      if (localDate != null) {
        jsonGenerator.writeNumber(localDate.atTime(0,0).toInstant(OffsetDateTime.now().getOffset()).toEpochMilli());
      } else {
        jsonGenerator.writeNull();
      }
    }
  }
}
