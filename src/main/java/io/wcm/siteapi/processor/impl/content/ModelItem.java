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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.Strings;

/**
 * Represents an item in a container item list, and its subitem in the defined order.
 */
class ModelItem {

  static final String ROOT_PATH = "/";
  static final String PN_ITEMS = ":items";
  static final String PN_ITEMSORDER = ":itemsOrder";

  private final String path;
  private final Map<String, Object> properties;
  private final Map<String, ModelItem> items;

  /**
   * @param path Absolute path relative to jcr:content node
   * @param data Raw JSON data from model.json
   */
  ModelItem(String path, Map<String, Object> data) {
    this.path = path;
    this.properties = getProperties(data);
    this.items = getModelItems(data, path);
  }

  private static Map<String, Object> getProperties(Map<String, Object> data) {
    return data.entrySet().stream()
      .filter(entry -> !Strings.CS.equalsAny(entry.getKey(), PN_ITEMS, PN_ITEMSORDER))
      .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
  }

  private static Map<String, ModelItem> getModelItems(Map<String, Object> data, String parentPath) {
    Map<String, Object> itemsMap = getMap(data, PN_ITEMS);
    List<String> itemsOrder = getList(data, PN_ITEMSORDER);

    Map<String, ModelItem> items = new LinkedHashMap<>();
    itemsOrder.forEach(name -> {
      Map<String, Object> itemMap = getMap(itemsMap, name);
      if (!itemMap.isEmpty()) {
        items.put(name, new ModelItem(buildPath(parentPath, name), itemMap));
      }
    });
    return items;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> getMap(Map<String, Object> data, String property) {
    Object items = data.get(property);
    if (items instanceof Map) {
      return (Map<String, Object>)items;
    }
    return Collections.emptyMap();
  }

  @SuppressWarnings("unchecked")
  private static List<String> getList(Map<String, Object> data, String property) {
    Object itemsOrder = data.get(property);
    if (itemsOrder instanceof List) {
      return (List<String>)itemsOrder;
    }
    return Collections.emptyList();
  }

  private static String buildPath(String parentPath, String name) {
    StringBuilder path = new StringBuilder();
    if (!Strings.CS.equals(parentPath, ROOT_PATH)) {
      path.append(parentPath);
    }
    path.append("/").append(name);
    return path.toString();
  }

  /**
   * @return Absolute path relative to jcr:content node
   */
  public String getPath() {
    return this.path;
  }

  /**
   * @return Properties and nested data structures without child items
   */
  Map<String, Object> getProperties() {
    return this.properties;
  }

  /**
   * @return Child items in defined order
   */
  Map<String, ModelItem> getItems() {
    return this.items;
  }

  /**
   * @return Raw JSON data as in model.json
   */
  Map<String, Object> toJson() {
    Map<String, Object> data = new HashMap<>(this.properties);
    Map<String, Object> itemsMap = new HashMap<>();
    for (Map.Entry<String, ModelItem> entry : this.items.entrySet()) {
      itemsMap.put(entry.getKey(), entry.getValue().toJson());
    }
    if (!itemsMap.isEmpty()) {
      data.put(PN_ITEMS, itemsMap);
      data.put(PN_ITEMSORDER, List.copyOf(this.items.keySet()));
    }
    return data;
  }

}
