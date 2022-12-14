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
package io.wcm.siteapi.processor.caconfig.impl;

import static java.util.function.Predicate.not;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Context-Aware configuration singleton item. May contain nested context-aware configurations.
 */
class ConfigSingletonItem implements ConfigItem<SortedMap<String, Object>> {

  private final SortedMap<String, Object> data = new TreeMap<>();
  private final Set<String> requiredPropertyNames = new HashSet<>();

  /**
   * Add configuration property
   * @param key Key
   * @param value Value
   */
  public void put(@NotNull String key, @NotNull Object value) {
    this.data.put(key, value);
  }

  /**
   * Add property name that is marked as mandatory.
   * @param key Key
   */
  public void addRequiredPropertyName(@NotNull String key) {
    this.requiredPropertyNames.add(key);
  }

  @Override
  public boolean isEmpty() {
    return data.isEmpty();
  }

  @Override
  public boolean isValid() {
    // ensure all mandatory properties are set
    if (requiredPropertyNames.stream().anyMatch(this::isValueMissing)) {
      return false;
    }

    // ensure all nested configurations are valid
    return this.data.values().stream()
        .filter(ConfigItem.class::isInstance)
        .map(ConfigItem.class::cast)
        .noneMatch(not(ConfigItem::isValid));
  }

  private boolean isValueMissing(@NotNull String propertyName) {
    Object value = data.get(propertyName);
    if (value instanceof String) {
      return StringUtils.isEmpty((String)value);
    }
    return value == null;
  }


  @Override
  public @Nullable SortedMap<String, Object> toJsonObject() {
    if (!isValid()) {
      // skip invalid items
      return null;
    }
    // transform nested ConfigItems to JSON structure
    SortedMap<String, Object> result = new TreeMap<>();
    data.entrySet().forEach(entry -> {
      String key = entry.getKey();
      Object value = entry.getValue();
      if (value instanceof ConfigItem) {
        value = ((ConfigItem)value).toJsonObject();
      }
      if (value != null) {
        result.put(key, value);
      }
    });
    return result;
  }

}
