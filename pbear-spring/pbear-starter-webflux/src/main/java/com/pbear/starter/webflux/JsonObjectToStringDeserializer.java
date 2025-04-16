package com.pbear.starter.webflux;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class JsonObjectToStringDeserializer extends JsonDeserializer<String> {
  @Override
  public String deserialize(final JsonParser jsonParser, final DeserializationContext deserializationContext) throws IOException {
    return jsonParser.readValueAsTree().toString();
  }
}
