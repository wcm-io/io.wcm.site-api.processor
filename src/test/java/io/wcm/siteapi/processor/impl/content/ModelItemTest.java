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

import static io.wcm.siteapi.processor.impl.content.JsonTestUtils.loadJson;
import static io.wcm.siteapi.processor.impl.content.ModelItem.PN_ITEMS;
import static io.wcm.siteapi.processor.impl.content.ModelItem.PN_ITEMSORDER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;

import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.siteapi.processor.util.JsonObjectMapper;
import io.wcm.siteapi.processor.util.impl.JsonObjectMapperImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class ModelItemTest {

  private AemContext context = AppAemContext.newAemContext();

  private JsonObjectMapper jsonObjectMapper;

  @BeforeEach
  void setUp() {
    jsonObjectMapper = context.registerInjectActivateService(JsonObjectMapperImpl.class);
  }

  @Test
  void testFromToJson() throws IOException, JSONException {
    Map<String, Object> json = loadJson("sample-model.json", jsonObjectMapper);

    ModelItem jsonRoot = new ModelItem("/", json);
    assertEquals("/", jsonRoot.getPath());
    assertEquals("en", jsonRoot.getProperties().get("language"));
    assertNull(jsonRoot.getProperties().get(PN_ITEMS));
    assertNull(jsonRoot.getProperties().get(PN_ITEMSORDER));
    assertEquals(List.of("root"), List.copyOf(jsonRoot.getItems().keySet()));

    ModelItem root = jsonRoot.getItems().get("root");
    assertEquals("/root", root.getPath());
    assertEquals(List.of("header", "container", "footer"), List.copyOf(root.getItems().keySet()));

    ModelItem container = root.getItems().get("container");
    assertEquals("/root/container", container.getPath());
    assertEquals(List.of("carousel", "container", "teaser", "container_1679842506"), List.copyOf(container.getItems().keySet()));

    String originalJson = jsonObjectMapper.toJsonString(json);
    String generatedJson = jsonObjectMapper.toJsonString(jsonRoot.toJson());
    JSONAssert.assertEquals(originalJson, generatedJson, true);
  }

}
