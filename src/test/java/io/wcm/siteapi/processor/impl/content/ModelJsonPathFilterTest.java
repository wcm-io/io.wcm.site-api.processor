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

import static io.wcm.siteapi.processor.impl.content.JsonTestUtils.assertNoPath;
import static io.wcm.siteapi.processor.impl.content.JsonTestUtils.assertPath;
import static io.wcm.siteapi.processor.impl.content.JsonTestUtils.loadJson;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.siteapi.processor.util.JsonObjectMapper;
import io.wcm.siteapi.processor.util.impl.JsonObjectMapperImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class ModelJsonPathFilterTest {

  private AemContext context = AppAemContext.newAemContext();

  private JsonObjectMapper jsonObjectMapper;

  @BeforeEach
  void setUp() {
    jsonObjectMapper = context.registerInjectActivateService(JsonObjectMapperImpl.class);
  }

  @Test
  void testNoFilter() {
    DocumentContext json = filterIncludeExclude(List.of(), List.of());

    assertPath(json, "$[':items'].root[':items'].container");
    assertPath(json, "$[':items'].root[':items'].header");
    assertPath(json, "$[':items'].root[':items'].footer");
    assertEquals(List.of("header", "container", "footer"), json.read("$[':items'].root[':itemsOrder']"), "itemsOrder");
  }

  @Test
  void testExclude() {
    DocumentContext json = filterIncludeExclude(
        List.of(),
        List.of("/root/header", "/root/footer"));

    assertPath(json, "$[':items'].root[':items'].container");
    assertNoPath(json, "$[':items'].root[':items'].header");
    assertNoPath(json, "$[':items'].root[':items'].footer");
    assertEquals(List.of("container"), json.read("$[':items'].root[':itemsOrder']"), "itemsOrder");
  }

  @Test
  void testExclude_RootPath() {
    DocumentContext json = filterIncludeExclude(
        List.of(),
        List.of("/", "/root/header", "/root/footer"));

    assertNoPath(json, "$[':items']");
    assertNoPath(json, "$[':itemsOrder']");
  }

  @Test
  void testInclude() {
    DocumentContext json = filterIncludeExclude(
        List.of("/root/header", "/root/footer", "/root/container/container/image_list", "/root/invalid"),
        List.of());

    assertNoPath(json, "$[':items'].root");
    assertPath(json, "$[':items'].header");
    assertPath(json, "$[':items'].footer");
    assertPath(json, "$[':items'].image_list");
    assertEquals(List.of("header", "footer", "image_list"), json.read("$[':itemsOrder']"), "itemsOrder");
  }

  @Test
  void testInclude_RootPath() {
    DocumentContext json = filterIncludeExclude(
        List.of("/", "/root/header", "/root/footer"),
        List.of());

    assertPath(json, "$[':items'].root[':items'].container");
    assertPath(json, "$[':items'].root[':items'].header");
    assertPath(json, "$[':items'].root[':items'].footer");
    assertEquals(List.of("header", "container", "footer"), json.read("$[':items'].root[':itemsOrder']"), "itemsOrder");
  }

  @Test
  void testExcludeAndInclude() {
    DocumentContext json = filterIncludeExclude(
        List.of("/root/header", "/root/footer", "/root/container/container/image_list", "/root/invalid"),
        List.of("/root/header", "/root/container"));

    assertNoPath(json, "$[':items'].root");
    assertNoPath(json, "$[':items'].header");
    assertPath(json, "$[':items'].footer");
    assertNoPath(json, "$[':items'].image_list");
    assertEquals(List.of("footer"), json.read("$[':itemsOrder']"), "itemsOrder");
  }

  private DocumentContext filterIncludeExclude(List<String> includePaths, List<String> excludePaths) {
    try {
      Map<String, Object> json = loadJson("sample-model.json", jsonObjectMapper);
      ModelJsonPathFilter filter = new ModelJsonPathFilter(includePaths, excludePaths);
      Map<String, Object> filteredJson = filter.filter(json);
      String jsonString = jsonObjectMapper.toJsonString(filteredJson);
      return JsonPath.parse(jsonString);
    }
    catch (IOException ex) {
      throw new RuntimeException(ex);
    }
  }

}
