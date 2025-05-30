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

import static io.wcm.siteapi.processor.util.impl.JsonTestData.POJO;
import static io.wcm.siteapi.processor.util.impl.JsonTestData.POJO_JSON;
import static io.wcm.siteapi.processor.util.impl.JsonTestData.POJO_MAP;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.skyscreamer.jsonassert.JSONAssert;

import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.siteapi.processor.util.JsonObjectMapper;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class JsonObjectMapperImplTest {

  private AemContext context = AppAemContext.newAemContext();

  private JsonObjectMapper underTest;

  @BeforeEach
  void setUp() {
    underTest = context.registerInjectActivateService(JsonObjectMapperImpl.class);
  }

  @Test
  void testToJsonString_Pojo() throws Exception {
    String json = underTest.toJsonString(POJO);
    JSONAssert.assertEquals(POJO_JSON, json, true);
  }

  @Test
  void testToJsonString_Maps() throws Exception {
    String json = underTest.toJsonString(POJO_MAP);
    JSONAssert.assertEquals(POJO_JSON, json, true);
  }

  @Test
  void testToMap_Pojo() throws Exception {
    Map<String, Object> map = underTest.toMap(POJO);
    assertEquals(POJO_MAP, map);
  }

  @Test
  void parseToMap_JsonString() throws Exception {
    Map<String, Object> map = underTest.parseToMap(POJO_JSON);
    assertEquals(POJO_MAP, map);
  }

}
