/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2022 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.siteapi.processor.util.impl;

import java.io.IOException;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;

import io.wcm.siteapi.processor.util.JsonObjectMapper;

/**
 * Implements {@link JsonObjectMapper}.
 */
@Component(service = JsonObjectMapper.class)
public class JsonObjectMapperImpl implements JsonObjectMapper {

  private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder()
      // ensure consistent ordering of objects and properties
      .enable(MapperFeature.SORT_PROPERTIES_ALPHABETICALLY)
      .enable(SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS)
      // allow comments in JSON
      .enable(JsonParser.Feature.ALLOW_COMMENTS)
      // ignore null properties
      .serializationInclusion(Include.NON_NULL)
      .build();

  private static final JavaType MAP_TYPE = OBJECT_MAPPER.getTypeFactory()
      .constructMapType(Map.class, String.class, Object.class);

  @Override
  @SuppressWarnings("null")
  public @NotNull Map<String, Object> toMap(@NotNull Object object) {
    return OBJECT_MAPPER.convertValue(object, MAP_TYPE);
  }

  @Override
  public @NotNull String toJsonString(@NotNull Object object) throws IOException {
    return OBJECT_MAPPER.writeValueAsString(object);
  }

  @Override
  @SuppressWarnings("null")
  public @NotNull Map<String, Object> parseToMap(@NotNull String jsonString) throws IOException {
    return OBJECT_MAPPER.readValue(jsonString, MAP_TYPE);
  }

}
