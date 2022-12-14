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
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.testing.mock.caconfig.MockContextAwareConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationMapper;
import io.wcm.siteapi.processor.caconfig.impl.property.JsonRawValuePropertyMapper;
import io.wcm.siteapi.processor.caconfig.impl.sample.ConfigSample;
import io.wcm.siteapi.processor.caconfig.impl.sample.ConfigSampleList;
import io.wcm.siteapi.processor.caconfig.impl.sample.ConfigSampleNested;
import io.wcm.siteapi.processor.caconfig.impl.sample.ConfigSampleNoDefault;
import io.wcm.siteapi.processor.caconfig.impl.sample.ConfigSampleValidation;
import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.siteapi.processor.util.impl.JsonObjectMapperImpl;
import io.wcm.siteapi.processor.util.impl.JsonTestData;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class ContextAwareConfigurationMapperImplTest {

  private static final String ROOT_PATH = "/content/test";

  private AemContext context = AppAemContext.newAemContext();

  private ContextAwareConfigurationMapper underTest;

  @BeforeEach
  void setUp() {
    MockContextAwareConfig.registerAnnotationClasses(context,
        ConfigSample.class, ConfigSampleList.class, ConfigSampleNested.class, ConfigSampleNoDefault.class);
    context.currentPage(context.create().page(ROOT_PATH));

    context.registerInjectActivateService(JsonObjectMapperImpl.class);
    context.registerInjectActivateService(JsonRawValuePropertyMapper.class);
    underTest = context.registerInjectActivateService(ContextAwareConfigurationMapperImpl.class);
  }

  @Test
  void testPrimitiveTypes() {
    Map<String, Object> data = Map.of(
        "stringParam", "value1",
        "stringArrayParam", new String[] { "v1", "v2" },
        "intParam", 42,
        "intArrayParam", new Integer[] { 42, 43, 44 },
        "doubleParam", 1.23d,
        "doubleArrayParam", new Double[] { 1.23d, 2.34d, 3.45d },
        "boolParam", true,
        "boolArrayParam", new Boolean[] { true, false, true });
    MockContextAwareConfig.writeConfiguration(context, ROOT_PATH, ConfigSample.class, data);

    assertEquals(data,
        underTest.get(ConfigSample.class.getName(), context.request()));
  }

  @Test
  void testEmtpy() {
    assertNull(underTest.get(ConfigSampleNoDefault.class.getName(), context.request()));
  }

  @Test
  void testHiddenParameter() {
    MockContextAwareConfig.writeConfiguration(context, ROOT_PATH, ConfigSample.class,
        "stringHiddenParam", "valueHidden");

    assertEquals(Map.of("stringParam", "default"),
        underTest.get(ConfigSample.class.getName(), context.request()));
  }

  @Test
  void testJson_invalidJsonValue() {
    MockContextAwareConfig.writeConfiguration(context, ROOT_PATH, ConfigSample.class,
        "jsonValue", "invalid json");

    assertEquals(Map.of("stringParam", "default"),
        underTest.get(ConfigSample.class.getName(), context.request()));
  }

  @Test
  void testJson_emptyString() {
    MockContextAwareConfig.writeConfiguration(context, ROOT_PATH, ConfigSample.class,
        "jsonValue", "");

    assertEquals(Map.of("stringParam", "default"),
        underTest.get(ConfigSample.class.getName(), context.request()));
  }

  @Test
  void testJson_validValue() {
    MockContextAwareConfig.writeConfiguration(context, ROOT_PATH, ConfigSample.class,
        "jsonValue", JsonTestData.POJO_JSON);

    assertEquals(Map.of("stringParam", "default",
        "jsonValue", JsonTestData.POJO_MAP),
        underTest.get(ConfigSample.class.getName(), context.request()));
  }

  @Test
  void testJson_emptyJson() {
    MockContextAwareConfig.writeConfiguration(context, ROOT_PATH, ConfigSample.class,
        "jsonValue", "{}");

    assertEquals(Map.of("stringParam", "default",
        "jsonValue", Map.of()),
        underTest.get(ConfigSample.class.getName(), context.request()));
  }

  @Test
  void testJson_multipleValidValues() {
    MockContextAwareConfig.writeConfiguration(context, ROOT_PATH, ConfigSample.class,
        "jsonValues", new String[] {
            JsonTestData.POJO_JSON,
            "{\"param1\":\"value1\"}"
        });

    assertEquals(Map.of("stringParam", "default",
        "jsonValues", List.of(JsonTestData.POJO_MAP, Map.of("param1", "value1"))),
        underTest.get(ConfigSample.class.getName(), context.request()));
  }

  @Test
  void testJson_multipleValidInvalidValues() {
    MockContextAwareConfig.writeConfiguration(context, ROOT_PATH, ConfigSample.class,
        "jsonValues", new String[] {
            "{\"invalid-json",
            JsonTestData.POJO_JSON
        });

    assertEquals(Map.of("stringParam", "default",
        "jsonValues", List.of(JsonTestData.POJO_MAP)),
        underTest.get(ConfigSample.class.getName(), context.request()));
  }

  @Test
  void testList() {
    MockContextAwareConfig.writeConfigurationCollection(context, ROOT_PATH, ConfigSampleList.class, List.of(
        Map.of("stringParam", "value1", "intParam", 1, "boolParam", true),
        Map.of("stringParam", "value2")));

    assertEquals(List.of(
        Map.of("stringParam", "value1", "intParam", 1, "boolParam", true),
        Map.of("stringParam", "value2", "intParam", 5)),
        underTest.get(ConfigSampleList.class.getName(), context.request()));
  }

  @Test
  void testListEmpty() {
    assertNull(underTest.get(ConfigSampleList.class.getName(), context.request()));
  }

  @Test
  void testNested() {
    MockContextAwareConfig.writeConfiguration(context, ROOT_PATH, ConfigSampleNested.class,
        "stringParam", "value1");
    String[] valueArray = new String[] { "v1" };

    // writing complex nested configuration is not yet fully supported in a convenient way by MockContextAwareConfig
    // so we have to build the /conf persistence structure ourself
    String confRootPath = StringUtils.replace(ROOT_PATH, "/content/", "/conf/") + "/sling:configs/" + ConfigSampleNested.class.getName();
    context.create().resource(confRootPath + "/sub/sub_1", "subStringParam", "sub_1");
    context.create().resource(confRootPath + "/sub/sub_2", "subStringParam", "sub_2", "stringArrayParam", valueArray);
    context.create().resource(confRootPath + "/sub2", "sub2StringParam", "sub2");

    assertEquals(Map.of("stringParam", "value1",
        "sub", List.of(
            Map.of("subStringParam", "sub_1"),
            Map.of("subStringParam", "sub_2", "stringArrayParam", valueArray)),
        "sub2", Map.of("sub2StringParam", "sub2", "sub", Map.of())),
        underTest.get(ConfigSampleNested.class.getName(), context.request()));
  }

  @Test
  void testIgnoreSystemProperties() {
    // writing configuration directly to repository including some system properties
    String confRootPath = StringUtils.replace(ROOT_PATH, "/content/", "/conf/") + "/sling:configs/" + ConfigSample.class.getName();
    context.create().resource(confRootPath,
        "stringParam", "value1",
        "sling:resourceType", "/any/path",
        "jcr:primaryType", "nt:unstructured",
        "cq:lastModifiedBy", "admin");

    assertEquals(Map.of("stringParam", "value1"),
        underTest.get(ConfigSample.class.getName(), context.request()));
  }

  @Test
  void testInvalid() {
    MockContextAwareConfig.writeConfiguration(context, ROOT_PATH, ConfigSampleValidation.class,
        "stringArrayParam", new String[] { "v1", "v2" },
        "intArrayParam", new Integer[] { 42, 43, 44 });

    assertNull(underTest.get(ConfigSampleValidation.class.getName(), context.request()));
  }

}
