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

import org.junit.jupiter.api.Test;

class ConfigCollectionItemTest {

  @Test
  void testCollection() {
    ConfigCollectionItem underTest = new ConfigCollectionItem();

    ConfigSingletonItem item1 = new ConfigSingletonItem();
    item1.put("p1", "value1");
    underTest.addItem(item1);
    ConfigSingletonItem item2 = new ConfigSingletonItem();
    item2.put("p1", "value2");
    underTest.addItem(item2);

    ConfigSingletonItem invalidItem3 = new ConfigSingletonItem();
    invalidItem3.addRequiredPropertyName("p1");
    underTest.addItem(invalidItem3);

    assertEquals(
        List.of(Map.of("p1", "value1"), Map.of("p1", "value2")),
        underTest.toJsonObject());
  }

  @Test
  void testIsValid() {
    ConfigCollectionItem underTest = new ConfigCollectionItem();

    ConfigSingletonItem item1 = new ConfigSingletonItem();
    item1.addRequiredPropertyName("p1");
    underTest.addItem(item1);
    ConfigSingletonItem item2 = new ConfigSingletonItem();
    item2.addRequiredPropertyName("p2");
    underTest.addItem(item2);

    assertFalse(underTest.isValid());

    item1.put("p1", "value1");
    assertFalse(underTest.isValid());

    item2.put("p2", "value2");
    assertTrue(underTest.isValid());
  }

}
