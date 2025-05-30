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
package io.wcm.siteapi.processor.impl.index;

import static io.wcm.siteapi.processor.ProcessorConstants.PROCESSOR_INDEX;
import static io.wcm.siteapi.processor.textcontext.SiteApiTestUtil.processorRequestContext;
import static io.wcm.siteapi.processor.textcontext.SiteApiTestUtil.registerSiteApiServlet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.siteapi.processor.ProcessorRequestContext;
import io.wcm.siteapi.processor.impl.ProcessorManagerImpl;
import io.wcm.siteapi.processor.impl.content.ContentProcessor;
import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.siteapi.processor.url.impl.SimpleUrlBuilder;
import io.wcm.siteapi.processor.util.impl.JsonObjectMapperImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class IndexProcessorTest {

  private AemContext context = AppAemContext.newAemContext();

  private IndexProcessor underTest;

  @BeforeEach
  void setUp() {
    context.registerInjectActivateService(JsonObjectMapperImpl.class);
    context.registerInjectActivateService(ProcessorManagerImpl.class);
    registerSiteApiServlet(context);
    context.registerInjectActivateService(SimpleUrlBuilder.class);

    underTest = context.registerInjectActivateService(IndexProcessor.class);
    context.currentPage(context.create().page("/content/test"));
  }

  @Test
  void testEmpty() throws Exception {
    ProcessorRequestContext processorRequestContext = processorRequestContext(context.request(), PROCESSOR_INDEX);

    Collection<ProcessorIndex> result = underTest.process(processorRequestContext);
    assertNotNull(result);
    assertTrue(result.isEmpty());
  }

  @Test
  void testWithProcessor() throws Exception {
    context.registerInjectActivateService(ContentProcessor.class);
    ProcessorRequestContext processorRequestContext = processorRequestContext(context.request(), PROCESSOR_INDEX);

    Collection<ProcessorIndex> result = underTest.process(processorRequestContext);
    assertNotNull(result);
    assertEquals(1, result.size());

    ProcessorIndex item = result.iterator().next();
    assertEquals("content", item.getSuffix());
    assertEquals("/content/test.site.api/content.json", item.getUrl());
  }

}
