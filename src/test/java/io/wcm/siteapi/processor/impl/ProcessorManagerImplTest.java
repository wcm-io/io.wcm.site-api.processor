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
package io.wcm.siteapi.processor.impl;

import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_ENABLED;
import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_SUFFIX;
import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_SUFFIX_PATTERN;
import static io.wcm.sling.commons.caservice.ContextAwareService.PROPERTY_CONTEXT_PATH_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.osgi.framework.Constants.SERVICE_RANKING;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.Resource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.siteapi.processor.JsonObjectProcessor;
import io.wcm.siteapi.processor.Processor;
import io.wcm.siteapi.processor.ProcessorManager;
import io.wcm.siteapi.processor.ProcessorMetadata;
import io.wcm.siteapi.processor.SlingHttpServletProcessor;
import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class ProcessorManagerImplTest {

  private AemContext context = AppAemContext.newAemContext();

  @Mock
  private SlingHttpServletProcessor processor1;
  @Mock
  private JsonObjectProcessor processor2;
  @Mock
  private SlingHttpServletProcessor processor3;
  @Mock
  private SlingHttpServletProcessor processor4;

  private Resource resource;

  @BeforeEach
  void setUp() {
    resource = context.create().resource("/content/test");
  }

  @Test
  void testNoServices() {
    ProcessorManager underTest = context.registerInjectActivateService(ProcessorManagerImpl.class);
    assertNull(underTest.getMatching("suffix1", resource));
  }

  @Test
  void testServicesWithInvalidProperties() {
    context.registerService(Processor.class, processor1);
    context.registerService(Processor.class, processor2,
        PROPERTY_SUFFIX, "");
    context.registerService(Processor.class, processor3,
        PROPERTY_SUFFIX, "suffix1",
        PROPERTY_SUFFIX_PATTERN, "(invalid");
    ProcessorManager underTest = context.registerInjectActivateService(ProcessorManagerImpl.class);

    assertNull(underTest.getMatching("suffix1", resource));

    assertTrue(underTest.getAll(resource).collect(Collectors.toList()).isEmpty());
  }

  @Test
  void testMultipleServices() {
    context.registerService(Processor.class, processor1,
        PROPERTY_SUFFIX, "suffix1",
        SERVICE_RANKING, 300);
    context.registerService(Processor.class, processor2,
        PROPERTY_SUFFIX, "suffix2",
        PROPERTY_SUFFIX_PATTERN, "^suffix2(/.*)?$",
        SERVICE_RANKING, 200);
    context.registerService(Processor.class, processor3,
        PROPERTY_SUFFIX, "suffix3",
        PROPERTY_SUFFIX_PATTERN, "^suffix3$",
        SERVICE_RANKING, 100);

    ProcessorManager underTest = context.registerInjectActivateService(ProcessorManagerImpl.class);

    assertSame(processor1, underTest.getMatching("suffix1", resource));
    assertSame(processor2, underTest.getMatching("suffix2", resource));
    assertSame(processor2, underTest.getMatching("suffix2/sub1", resource));
    assertSame(processor3, underTest.getMatching("suffix3", resource));

    assertEquals(List.of("suffix1", "suffix2", "suffix3"),
        List.copyOf(underTest.getAll(resource).map(ProcessorMetadata::getSuffix).collect(Collectors.toList())));
  }

  @Test
  void testDisabled() {
    context.registerService(Processor.class, processor1,
        PROPERTY_SUFFIX, "enabled1",
        SERVICE_RANKING, 400,
        PROPERTY_ENABLED, true);
    context.registerService(Processor.class, processor2,
        PROPERTY_SUFFIX, "disabled2",
        SERVICE_RANKING, 300,
        PROPERTY_ENABLED, false);
    context.registerService(Processor.class, processor3,
        PROPERTY_SUFFIX, "enabled3",
        SERVICE_RANKING, 200);
    context.registerService(Processor.class, processor4,
        PROPERTY_SUFFIX, "disabled4",
        SERVICE_RANKING, 100,
        PROPERTY_ENABLED, "false");

    ProcessorManager underTest = context.registerInjectActivateService(ProcessorManagerImpl.class);

    assertSame(processor1, underTest.getMatching("enabled1", resource));
    assertNull(underTest.getMatching("disabled2", resource));
    assertSame(processor3, underTest.getMatching("enabled3", resource));
    assertNull(underTest.getMatching("disabled4", resource));

    assertEquals(List.of("enabled1", "enabled3"),
        List.copyOf(underTest.getAll(resource).map(ProcessorMetadata::getSuffix).collect(Collectors.toList())));
  }

  @Test
  void testContextAware() {
    context.registerService(Processor.class, processor1,
        PROPERTY_SUFFIX, "suffix1",
        PROPERTY_CONTEXT_PATH_PATTERN, "^/content(/.*)?$",
        SERVICE_RANKING, 300);
    context.registerService(Processor.class, processor2,
        PROPERTY_SUFFIX, "suffix2",
        PROPERTY_SUFFIX_PATTERN, "^suffix2(/.*)?$",
        PROPERTY_CONTEXT_PATH_PATTERN, "^/content/test(/.*)?$",
        SERVICE_RANKING, 200);
    context.registerService(Processor.class, processor3,
        PROPERTY_SUFFIX, "suffix3",
        PROPERTY_SUFFIX_PATTERN, "^suffix3$",
        PROPERTY_CONTEXT_PATH_PATTERN, "^/content/test2(/.*)?$",
        SERVICE_RANKING, 100);

    ProcessorManager underTest = context.registerInjectActivateService(ProcessorManagerImpl.class);

    assertSame(processor1, underTest.getMatching("suffix1", resource));
    assertSame(processor2, underTest.getMatching("suffix2", resource));
    assertSame(processor2, underTest.getMatching("suffix2/sub1", resource));
    assertNull(underTest.getMatching("suffix3", resource));

    assertEquals(List.of("suffix1", "suffix2"),
        List.copyOf(underTest.getAll(resource).map(ProcessorMetadata::getSuffix).collect(Collectors.toList())));

    Resource resource2 = context.create().resource("/content/test2");

    assertSame(processor1, underTest.getMatching("suffix1", resource2));
    assertNull(underTest.getMatching("suffix2", resource2));
    assertNull(underTest.getMatching("suffix2/sub1", resource2));
    assertSame(processor3, underTest.getMatching("suffix3", resource2));

    assertEquals(List.of("suffix1", "suffix3"),
        List.copyOf(underTest.getAll(resource2).map(ProcessorMetadata::getSuffix).collect(Collectors.toList())));
  }

  @Test
  void testProcessorMetadata() {
    context.registerService(Processor.class, processor1,
        PROPERTY_SUFFIX, "suffix1",
        SERVICE_RANKING, 100);

    ProcessorManager underTest = context.registerInjectActivateService(ProcessorManagerImpl.class);
    ProcessorMetadata metadata = underTest.getAll(resource)
        .filter(item -> StringUtils.equals(item.getSuffix(), "suffix1"))
        .findFirst().get();

    assertEquals("suffix1", metadata.getSuffix());
    assertEquals(processor1.getClass(), metadata.getProcessorClass());
    assertEquals("suffix1", metadata.getProperties().get(PROPERTY_SUFFIX));
    assertEquals(100, metadata.getProperties().get(SERVICE_RANKING));
    assertNotNull(metadata.toString());
  }

}
