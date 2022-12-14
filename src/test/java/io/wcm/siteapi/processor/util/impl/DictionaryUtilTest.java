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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import org.junit.jupiter.api.Test;

class DictionaryUtilTest {

  @Test
  void testToMap() {
    Dictionary<String, Object> dictionary = new Hashtable<>();
    dictionary.put("prop1", "value1");
    dictionary.put("prop2", 42);

    assertEquals(Map.of("prop1", "value1", "prop2", 42), DictionaryUtil.toMap(dictionary));
  }

}
