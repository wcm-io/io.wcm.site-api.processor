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
package io.wcm.siteapi.processor.util;

import java.io.IOException;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Parsing and serialization of objects to JSON and Maps.
 */
@ProviderType
public interface JsonObjectMapper {

  /**
   * Convert POJO object to nested map structure.
   * @param object Java POJO
   * @return Map with structured data
   */
  @NotNull
  Map<String, Object> toMap(@NotNull Object object);

  /**
   * Serialize java object to JSON.
   * @param object Java object or Map structure
   * @return JSON string
   * @throws IOException If JSON serialization fails.
   */
  @NotNull
  String toJsonString(@NotNull Object object) throws IOException;

  /**
   * Parses a JSON string and returns a nested map structure.
   * @param jsonString JSON string
   * @return Map with structured data
   * @throws IOException If JSON parsing fails.
   */
  @NotNull
  Map<String, Object> parseToMap(@NotNull String jsonString) throws IOException;

}
