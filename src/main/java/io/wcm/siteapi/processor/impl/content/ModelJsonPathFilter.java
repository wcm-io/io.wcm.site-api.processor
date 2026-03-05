/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2023 wcm.io
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
package io.wcm.siteapi.processor.impl.content;

import static io.wcm.siteapi.processor.impl.content.ModelItem.PN_ITEMS;
import static io.wcm.siteapi.processor.impl.content.ModelItem.PN_ITEMSORDER;
import static io.wcm.siteapi.processor.impl.content.ModelItem.ROOT_PATH;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceUtil;

/**
 * Filters model.json data by include/exclude paths based on the container structure.
 * Excludes are applied first, then the includes.
 */
class ModelJsonPathFilter {

  private final List<String> includePaths;
  private final Set<String> excludePaths;

  ModelJsonPathFilter(String[] includePaths, String[] excludePaths) {
    this(Arrays.asList(includePaths), Arrays.asList(excludePaths));
  }

  ModelJsonPathFilter(List<String> includePaths, List<String> excludePaths) {
    this.includePaths = includePaths;
    this.excludePaths = Set.copyOf(excludePaths);
  }

  Map<String, Object> filter(Map<String, Object> content) {
    if (excludePaths.contains(ROOT_PATH)) {
      return Collections.emptyMap();
    }

    ModelItem root = new ModelItem(ROOT_PATH, content);
    applyExcludes(root);

    if (this.includePaths.isEmpty()) {
      return root.toJson();
    }
    else {
      return buildWithIncludes(root);
    }
  }

  /**
   * Removes all items matching any of the excluded paths.
   * @param item Item
   */
  private void applyExcludes(ModelItem item) {
    Map<String, ModelItem> items = item.getItems();
    List<String> namesToRemove = items.entrySet().stream()
      .filter(entry -> excludePaths.contains(entry.getValue().getPath()))
      .map(Map.Entry::getKey)
      .collect(Collectors.toList());
    namesToRemove.forEach(items::remove);
    items.values().forEach(this::applyExcludes);
  }

  /**
   * Rebuild response by collecting all elements to include as top-level elements (using name of the element).
   * @param root Root item
   * @return Response containing only the includes
   */
  private Map<String, Object> buildWithIncludes(ModelItem root) {
    if (includePaths.contains(ROOT_PATH)) {
      return root.toJson();
    }

    Map<String, Object> result = new HashMap<>();

    Map<String, Object> items = new LinkedHashMap<>();
    this.includePaths.stream()
      .map(path -> findByPath(root, path))
      .filter(Objects::nonNull)
      .forEach(item -> items.put(ResourceUtil.getName(item.getPath()), item.toJson()));

    result.put(PN_ITEMS, items);
    result.put(PN_ITEMSORDER, List.copyOf(items.keySet()));
    return result;
  }

  private ModelItem findByPath(ModelItem item, String path) {
    for (ModelItem child : item.getItems().values()) {
      if (StringUtils.equals(child.getPath(), path)) {
        return child;
      }
      ModelItem descendant = findByPath(child, path);
      if (descendant != null) {
        return descendant;
      }
    }
    return null;
  }

}
