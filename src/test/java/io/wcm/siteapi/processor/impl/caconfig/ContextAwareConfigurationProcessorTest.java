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
package io.wcm.siteapi.processor.impl.caconfig;

import static io.wcm.siteapi.processor.ProcessorConstants.PROCESSOR_CONFIG;
import static io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationProperties.PROPERTY_CONFIG_EMBEDDED;
import static io.wcm.siteapi.processor.impl.caconfig.ContextAwareConfigurationProcessor.toConfigNameWithoutPrefix;
import static io.wcm.siteapi.processor.textcontext.SiteApiTestUtil.processorRequestContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.osgi.framework.Constants.SERVICE_RANKING;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mock.Strictness;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationMapper;
import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.siteapi.processor.url.UrlBuilder;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class ContextAwareConfigurationProcessorTest {

  private AemContext context = AppAemContext.newAemContext();

  @Mock(strictness = Strictness.LENIENT)
  private UrlBuilder urlBuilder;
  @Mock(strictness = Strictness.LENIENT)
  private ConfigurationManager configManager;
  @Mock(strictness = Strictness.LENIENT)
  private ContextAwareConfigurationMapper configMapper;

  private static final String CONFIG_1_NAME = "x.y.z.Config1";
  private static final Map<String, Object> CONFIG_1 = Map.of(
      "param1", "value1",
      "param2", 42);
  private static final String CONFIG_COLLECTION_2_NAME = "ConfigCollection2";
  private static final Collection<Map<String, Object>> CONFIG_COLLECTION_2 = List.of(
      Map.of("param1", "value1"),
      Map.of("param2", "value2"));
  private static final String CONFIG_3_NOT_EMBED_NAME = "x.y.z.Config3";

  @BeforeEach
  void setUp() {
    context.registerService(ConfigurationManager.class, configManager,
        SERVICE_RANKING, 100);
    context.registerService(ContextAwareConfigurationMapper.class, configMapper);
    context.registerService(UrlBuilder.class, urlBuilder);

    context.currentPage(context.create().page("/content/test"));

    when(configMapper.get(CONFIG_1_NAME, context.request())).thenReturn(CONFIG_1);
    when(configMapper.get(CONFIG_COLLECTION_2_NAME, context.request())).thenReturn(CONFIG_COLLECTION_2);
    when(configMapper.get(CONFIG_3_NOT_EMBED_NAME, context.request())).thenReturn(Map.of());
  }

  @Test
  void testNoConfig() {
    assertEquals(Map.of(), process());
  }

  @Test
  void testNonExistingConfig() {
    context.registerInjectActivateService(ContextAwareConfigurationExport.class,
        "names", new String[] {
            "non-existing-config-1",
            "non-existing-config-2",
        });
    assertEquals(Map.of(), process());

    verify(configMapper).get("non-existing-config-1", context.request());
    verify(configMapper).get("non-existing-config-2", context.request());
  }

  @Test
  void testAllConfig() {
    context.registerInjectActivateService(ContextAwareConfigurationExport.class,
        "names", new String[] {
            CONFIG_1_NAME,
            CONFIG_COLLECTION_2_NAME,
        });
    assertEquals(Map.of("Config1", CONFIG_1, CONFIG_COLLECTION_2_NAME, CONFIG_COLLECTION_2), process());
  }

  @Test
  void testAllConfig_NoShortening() {
    context.registerInjectActivateService(ContextAwareConfigurationExport.class,
        "names", new String[] {
            CONFIG_1_NAME,
            CONFIG_COLLECTION_2_NAME,
        });
    assertEquals(Map.of(CONFIG_1_NAME, CONFIG_1, CONFIG_COLLECTION_2_NAME, CONFIG_COLLECTION_2), process(
        "shortenConfigNames", false));
  }

  @Test
  void testAllConfig_NotEmbedded() {
    ConfigurationMetadata config3Metadata = new ConfigurationMetadata(CONFIG_3_NOT_EMBED_NAME, List.of(), false);
    config3Metadata.properties(Map.of(PROPERTY_CONFIG_EMBEDDED, "false"));
    when(configManager.getConfigurationMetadata(CONFIG_3_NOT_EMBED_NAME)).thenReturn(config3Metadata);

    when(urlBuilder.build(any(), eq(PROCESSOR_CONFIG), eq("Config3"), any())).thenReturn("/dummy.json");

    context.registerInjectActivateService(ContextAwareConfigurationExport.class,
        "names", new String[] {
            CONFIG_1_NAME,
            CONFIG_COLLECTION_2_NAME,
            CONFIG_3_NOT_EMBED_NAME
        });
    assertEquals(Map.of("Config1", CONFIG_1, CONFIG_COLLECTION_2_NAME, CONFIG_COLLECTION_2,
        "Config3:link", Map.of("url", "/dummy.json")), process());
  }

  @Test
  void testSingleConfig() {
    context.registerInjectActivateService(ContextAwareConfigurationExport.class,
        "names", new String[] {
            CONFIG_1_NAME,
            CONFIG_COLLECTION_2_NAME,
        });
    assertEquals(CONFIG_1, processWithSuffixExtension("Config1"));
  }

  @Test
  void testSingleConfigCollection() {
    context.registerInjectActivateService(ContextAwareConfigurationExport.class,
        "names", new String[] {
            CONFIG_1_NAME,
            CONFIG_COLLECTION_2_NAME,
        });
    assertEquals(CONFIG_COLLECTION_2, processWithSuffixExtension(CONFIG_COLLECTION_2_NAME));
  }

  @Test
  void testToConfigNameWithoutPrefix() {
    assertEquals("MyClass", toConfigNameWithoutPrefix("x.y.z.MyClass"));
    assertEquals("MyName", toConfigNameWithoutPrefix("MyName"));
  }

  private Object process(Object... serviceProps) {
    return processWithSuffixExtension(null, serviceProps);
  }

  private Object processWithSuffixExtension(@Nullable String suffixExtension, Object... serviceProps) {
    ContextAwareConfigurationProcessor underTest = context.registerInjectActivateService(
        ContextAwareConfigurationProcessor.class, serviceProps);
    return underTest.process(processorRequestContext(context.request(), PROCESSOR_CONFIG, suffixExtension));
  }

}
