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
    underTest.put("p1", "value1");
    underTest.put("p2", true);
    underTest.put("p3", 42);
    underTest.put("p4", 1.5);

    assertEquals(new TreeMap<>(Map.of(
        "p1", "value1",
        "p2", true,
        "p3", 42,
        "p4", 1.5)),
        underTest.toJsonObject());
  }

  @Test
  void testNestedItemsCollections() {
    ConfigSingletonItem underTest = new ConfigSingletonItem();
    underTest.put("p1", "value1");

    ConfigSingletonItem child1 = new ConfigSingletonItem();
    child1.put("p1", "value1.1");
    underTest.put("child1", child1);
    ConfigSingletonItem child2 = new ConfigSingletonItem();
    child2.put("p1", "value1.2");
    underTest.put("child2", child2);

    ConfigCollectionItem childCollection1 = new ConfigCollectionItem();
    ConfigSingletonItem item1 = new ConfigSingletonItem();
    item1.put("p1", "value2.1");
    childCollection1.addItem(item1);
    ConfigSingletonItem item2 = new ConfigSingletonItem();
    item2.put("p1", "value2.2");
    childCollection1.addItem(item2);
    underTest.put("col1", childCollection1);

    assertEquals(new TreeMap<>(Map.of(
        "p1", "value1",
        "child1", Map.of("p1", "value1.1"),
        "child2", Map.of("p1", "value1.2"),
        "col1", List.of(Map.of("p1", "value2.1"), Map.of("p1", "value2.2")))),
        underTest.toJsonObject());
  }

  @Test
  void testNestedMaps() {
    ConfigSingletonItem underTest = new ConfigSingletonItem();
    underTest.put("p1", "value1");
    underTest.put("p2", Map.of("p21", "value1", "p22", true));
    underTest.put("p3", Map.of("p31", Map.of("p1", "value1"), "p32", Map.of("p1", "value2")));
    underTest.put("p4", List.of(Map.of("p51", "value1"), Map.of("p52", "value2")));

    assertEquals(new TreeMap<>(Map.of(
        "p1", "value1",
        "p2", Map.of("p21", "value1", "p22", true),
        "p3", Map.of("p31", Map.of("p1", "value1"), "p32", Map.of("p1", "value2")),
        "p4", List.of(Map.of("p51", "value1"), Map.of("p52", "value2")))),
        underTest.toJsonObject());
  }

  @Test
  void testIsValid() {
    ConfigSingletonItem underTest = new ConfigSingletonItem();
    underTest.addRequiredPropertyName("p1");
    underTest.addRequiredPropertyName("p3");

    underTest.put("p1", "value1");
    underTest.put("p2", true);
    assertFalse(underTest.isValid());

    // add missing property
    underTest.put("p3", 42);
    assertTrue(underTest.isValid());

    // empty string is treated as not set = invalid for required property
    underTest.put("p1", "");
    assertFalse(underTest.isValid());
  }

  @Test
  void testIsValid_ChildConfig() {
    ConfigSingletonItem underTest = new ConfigSingletonItem();

    underTest.put("p1", "value1");
    underTest.put("p2", true);

    ConfigSingletonItem child1 = new ConfigSingletonItem();
    child1.addRequiredPropertyName("p1");
    underTest.put("child1", child1);

    assertFalse(underTest.isValid());

    child1.put("p1", "value1");
    assertTrue(underTest.isValid());
  }

}
