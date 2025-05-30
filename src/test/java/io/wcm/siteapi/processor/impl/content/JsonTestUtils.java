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

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.PathNotFoundException;

import io.wcm.siteapi.processor.util.JsonObjectMapper;

final class JsonTestUtils {

  private JsonTestUtils() {
    // static methods only
  }

  static String loadJsonString(String classpathUri) throws IOException {
    try (InputStream is = JsonTestUtils.class.getClassLoader().getResourceAsStream(classpathUri)) {
      return IOUtils.toString(is, StandardCharsets.UTF_8);
    }
  }

  static Map<String, Object> loadJson(String classpathUri, JsonObjectMapper jsonObjectMapper) throws IOException {
    return jsonObjectMapper.parseToMap(loadJsonString(classpathUri));
  }

  static void assertPath(DocumentContext json, String path) {
    try {
      json.read(path);
    }
    catch (PathNotFoundException ex) {
      fail("not found: " + path);
    }
  }

  static void assertNoPath(DocumentContext json, String path) {
    try {
      json.read(path);
      fail("unexpected: " + path);
    }
    catch (PathNotFoundException ex) {
      // expected
    }
  }

}
