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
package io.wcm.siteapi.processor.url;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class JsonSuffixTest {

  @Test
  void testValidSuffix() {
    JsonSuffix suffix = JsonSuffix.parse("/suffix1.json");
    assertNotNull(suffix);
    assertEquals("suffix1", suffix.getSuffix());
    assertNull(suffix.getSuffixExtension());
  }

  @Test
  void testValidSuffixWithExtension() {
    JsonSuffix suffix = JsonSuffix.parse("/suffix1/sub1.json");
    assertNotNull(suffix);
    assertEquals("suffix1", suffix.getSuffix());
    assertEquals("sub1", suffix.getSuffixExtension());
  }

  @Test
  void testInvalidSuffix() {
    assertNull(JsonSuffix.parse("/suffix1.txt"));
    assertNull(JsonSuffix.parse("/suffix1"));
    assertNull(JsonSuffix.parse("suffix1"));
    assertNull(JsonSuffix.parse("/"));
    assertNull(JsonSuffix.parse(""));
    assertNull(JsonSuffix.parse(null));
  }

  @Test
  void testBuild() {
    assertEquals("/suffix1.json", JsonSuffix.build("suffix1"));
    assertEquals("/suffix1/sub1.json", JsonSuffix.build("suffix1", "sub1"));
  }

}
