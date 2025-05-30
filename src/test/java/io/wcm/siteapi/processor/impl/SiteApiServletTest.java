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

import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_SUFFIX;
import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_SUFFIX_PATTERN;
import static io.wcm.siteapi.processor.textcontext.AppAemContext.API_EXTENSION;
import static io.wcm.siteapi.processor.textcontext.AppAemContext.API_SELECTOR;
import static io.wcm.siteapi.processor.textcontext.SiteApiTestUtil.registerSiteApiServlet;
import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.apache.sling.servlethelpers.MockRequestPathInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.skyscreamer.jsonassert.JSONAssert;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.WCMMode;

import io.wcm.siteapi.processor.JsonObjectProcessor;
import io.wcm.siteapi.processor.Processor;
import io.wcm.siteapi.processor.ProcessorRequestContext;
import io.wcm.siteapi.processor.SlingHttpServletProcessor;
import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.siteapi.processor.util.impl.JsonObjectMapperImpl;
import io.wcm.siteapi.processor.util.impl.JsonTestData;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class SiteApiServletTest {

  private AemContext context = AppAemContext.newAemContext();

  private SiteApiServlet underTest;

  @Mock
  private SlingHttpServletProcessor processor1;
  @Mock
  private JsonObjectProcessor processor2;

  @BeforeEach
  void setUp() throws Exception {
    context.registerService(Processor.class, processor1,
        PROPERTY_SUFFIX, "suffix1");
    context.registerService(Processor.class, processor2,
        PROPERTY_SUFFIX, "suffix2",
        PROPERTY_SUFFIX_PATTERN, "^suffix2(/.*)?$");

    context.registerInjectActivateService(JsonObjectMapperImpl.class);
    context.registerInjectActivateService(ProcessorManagerImpl.class);
    underTest = registerSiteApiServlet(context);
  }

  @Test
  void testValidCall_processor1() throws Exception {
    context.currentPage(context.create().page("/content/page1"));
    ((MockRequestPathInfo)context.request().getRequestPathInfo()).setSuffix("/suffix1.json");

    underTest.doGet(context.request(), context.response());

    assertEquals(WCMMode.DISABLED, WCMMode.fromRequest(context.request()));
    assertEquals(SC_OK, context.response().getStatus());
    verify(processor1, times(1)).process(any(ProcessorRequestContext.class), eq(context.response()));
    verifyNoInteractions(processor2);
  }

  @Test
  void testValidCall_processor2_jsonresponse() throws Exception {
    context.currentPage(context.create().page("/content/page1"));
    ((MockRequestPathInfo)context.request().getRequestPathInfo()).setSuffix("/suffix2/sub1/sub2.json");

    when(processor2.process(any(ProcessorRequestContext.class))).thenReturn(JsonTestData.POJO);
    underTest.doGet(context.request(), context.response());

    assertEquals(WCMMode.DISABLED, WCMMode.fromRequest(context.request()));
    assertEquals(SC_OK, context.response().getStatus());
    JSONAssert.assertEquals(JsonTestData.POJO_JSON, context.response().getOutputAsString(), true);
    verifyNoInteractions(processor1);
    verify(processor2, times(1)).process(any(ProcessorRequestContext.class));
  }

  @Test
  void testValidCall_processor2_noresponse() throws Exception {
    context.currentPage(context.create().page("/content/page1"));
    ((MockRequestPathInfo)context.request().getRequestPathInfo()).setSuffix("/suffix2/sub1/sub2.json");

    underTest.doGet(context.request(), context.response());

    assertEquals(WCMMode.DISABLED, WCMMode.fromRequest(context.request()));
    assertEquals(SC_NOT_FOUND, context.response().getStatus());
    verifyNoInteractions(processor1);
    verify(processor2, times(1)).process(any(ProcessorRequestContext.class));
  }

  @Test
  void testMissingPage() throws Exception {
    context.currentResource(context.create().resource("/content/page1"));
    ((MockRequestPathInfo)context.request().getRequestPathInfo()).setSuffix("/suffix1.json");

    underTest.doGet(context.request(), context.response());

    assertEquals(SC_NOT_FOUND, context.response().getStatus());
    verifyNoInteractions(processor1, processor2);
  }

  @Test
  @SuppressWarnings("null")
  void testMissingPageContent() throws Exception {
    Page page = context.currentPage(context.create().page("/content/page1"));
    context.resourceResolver().delete(page.getContentResource());
    ((MockRequestPathInfo)context.request().getRequestPathInfo()).setSuffix("/suffix1.json");

    underTest.doGet(context.request(), context.response());

    assertEquals(SC_NOT_FOUND, context.response().getStatus());
    verifyNoInteractions(processor1, processor2);
  }

  @Test
  void testMissingSuffix() throws Exception {
    context.currentPage(context.create().page("/content/page1"));

    underTest.doGet(context.request(), context.response());

    assertEquals(SC_NOT_FOUND, context.response().getStatus());
    verifyNoInteractions(processor1, processor2);
  }

  @Test
  void testInvalidSuffix() throws Exception {
    context.currentPage(context.create().page("/content/page1"));
    ((MockRequestPathInfo)context.request().getRequestPathInfo()).setSuffix("/suffix1");

    underTest.doGet(context.request(), context.response());

    assertEquals(SC_NOT_FOUND, context.response().getStatus());
    verifyNoInteractions(processor1, processor2);
  }

  @Test
  void testInvalidSelector() throws Exception {
    context.requestPathInfo().setSelectorString(API_SELECTOR + ".dummy");

    context.currentPage(context.create().page("/content/page1"));
    ((MockRequestPathInfo)context.request().getRequestPathInfo()).setSuffix("/suffix1.json");

    underTest.doGet(context.request(), context.response());

    assertEquals(SC_NOT_FOUND, context.response().getStatus());
    verifyNoInteractions(processor1, processor2);
  }

  @Test
  void testSiteApiConfiguration() {
    assertEquals(API_SELECTOR, underTest.getSelector());
    assertEquals(API_EXTENSION, underTest.getExtension());
    assertEquals("", underTest.getContextPath());
  }

}
