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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

class ConfigSingletonItemTest {

  @Test
  void testProperties() {
    ConfigSingletonItem underTest = new ConfigSingletonItem();
    underTest.put("prop1", "value1");
    underTest.put("prop2", true);
    underTest.put("prop3", 42);
    underTest.put("prop4", 1.5);

    assertEquals(new TreeMap<>(Map.of(
        "prop1", "value1",
        "prop2", true,
        "prop3", 42,
        "prop4", 1.5)),
        underTest.toJsonObject());
  }

  @Test
  void testNestedItemsCollections() {
    ConfigSingletonItem underTest = new ConfigSingletonItem();
    underTest.put("prop1", "value1");

    ConfigSingletonItem child1 = new ConfigSingletonItem();
    child1.put("prop1", "value1.1");
    underTest.put("child1", child1);
    ConfigSingletonItem child2 = new ConfigSingletonItem();
    child2.put("prop1", "value1.2");
    underTest.put("child2", child2);

    ConfigCollectionItem childCollection1 = new ConfigCollectionItem();
    ConfigSingletonItem item1 = new ConfigSingletonItem();
    item1.put("prop1", "value2.1");
    childCollection1.addItem(item1);
    ConfigSingletonItem item2 = new ConfigSingletonItem();
    item2.put("prop1", "value2.2");
    childCollection1.addItem(item2);
    underTest.put("col1", childCollection1);

    assertEquals(new TreeMap<>(Map.of(
        "prop1", "value1",
        "child1", Map.of("prop1", "value1.1"),
        "child2", Map.of("prop1", "value1.2"),
        "col1", List.of(Map.of("prop1", "value2.1"), Map.of("prop1", "value2.2")))),
        underTest.toJsonObject());
  }

  @Test
  void testNestedMaps() {
    ConfigSingletonItem underTest = new ConfigSingletonItem();
    underTest.put("prop1", "value1");
    underTest.put("prop2", Map.of("prop21", "value1", "prop22", true));
    underTest.put("prop3", Map.of("prop31", Map.of("prop1", "value1"), "prop32", Map.of("prop1", "value2")));
    underTest.put("prop4", List.of(Map.of("prop51", "value1"), Map.of("prop52", "value2")));

    assertEquals(new TreeMap<>(Map.of(
        "prop1", "value1",
        "prop2", Map.of("prop21", "value1", "prop22", true),
        "prop3", Map.of("prop31", Map.of("prop1", "value1"), "prop32", Map.of("prop1", "value2")),
        "prop4", List.of(Map.of("prop51", "value1"), Map.of("prop52", "value2")))),
        underTest.toJsonObject());
  }

  @Test
  void testIsValid() {
    ConfigSingletonItem underTest = new ConfigSingletonItem();
    underTest.addRequiredPropertyName("prop1");
    underTest.addRequiredPropertyName("prop3");

    underTest.put("prop1", "value1");
    underTest.put("prop2", true);
    assertFalse(underTest.isValid());

    // add missing property
    underTest.put("prop3", 42);
    assertTrue(underTest.isValid());

    // empty string is treated as not set = invalid for required property
    underTest.put("prop1", "");
    assertFalse(underTest.isValid());
  }

  @Test
  void testIsValid_ChildConfig() {
    ConfigSingletonItem underTest = new ConfigSingletonItem();

    underTest.put("prop1", "value1");
    underTest.put("prop2", true);

    ConfigSingletonItem child1 = new ConfigSingletonItem();
    child1.addRequiredPropertyName("prop1");
    underTest.put("child1", child1);

    assertFalse(underTest.isValid());

    child1.put("prop1", "value1");
    assertTrue(underTest.isValid());
  }

}
