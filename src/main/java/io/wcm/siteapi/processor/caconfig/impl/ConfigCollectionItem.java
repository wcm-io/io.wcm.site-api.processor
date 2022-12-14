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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

/**
 * Collection of context-aware configuration items.
 */
class ConfigCollectionItem implements ConfigItem<Collection<SortedMap<String, Object>>> {

  private final List<ConfigSingletonItem> childSingletonItems = new ArrayList<>();

  /**
   * Add singleton child item.
   * @param item Item
   */
  public void addItem(ConfigSingletonItem item) {
    this.childSingletonItems.add(item);
  }

  @Override
  public boolean isEmpty() {
    return childSingletonItems.isEmpty();
  }

  @Override
  public boolean isValid() {
    // check for invalid configuration items
    return childSingletonItems.stream()
        .noneMatch(Predicate.not(ConfigSingletonItem::isValid));
  }


  @Override
  public @Nullable Collection<SortedMap<String, Object>> toJsonObject() {
    // consider only valid items
    List<SortedMap<String, Object>> validItems = childSingletonItems.stream()
        .filter(ConfigSingletonItem::isValid)
        .map(ConfigSingletonItem::toJsonObject)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
    if (validItems.isEmpty()) {
      // skip empty collection item
      return null;
    }
    return validItems;
  }

}
