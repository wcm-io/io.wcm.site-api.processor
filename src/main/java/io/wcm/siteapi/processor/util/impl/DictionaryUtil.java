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

import java.util.Dictionary;
import java.util.Map;

import org.osgi.util.converter.Converters;
import org.osgi.util.converter.TypeReference;

/**
 * Converts dictionaries.
 */
public final class DictionaryUtil {

  private DictionaryUtil() {
    // static methods only
  }

  private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    // no impl
  };

  /**
   * Converts a {@link Dictionary} to a {@link Map}.
   * @param dictionary Dictionary
   * @return Map
   */
  public static Map<String, Object> toMap(Dictionary<String, Object> dictionary) {
    return Converters.standardConverter().convert(dictionary).to(MAP_TYPE);
  }

}
