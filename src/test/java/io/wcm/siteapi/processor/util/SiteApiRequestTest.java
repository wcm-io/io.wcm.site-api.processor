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
package io.wcm.siteapi.processor.util;

import static com.adobe.cq.export.json.ExporterConstants.SLING_MODEL_EXTENSION;
import static com.adobe.cq.export.json.ExporterConstants.SLING_MODEL_SELECTOR;
import static io.wcm.siteapi.processor.textcontext.SiteApiTestUtil.registerSiteApiServlet;
import static io.wcm.siteapi.processor.util.SiteApiRequest.isSiteApiRequest;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.siteapi.processor.impl.ProcessorManagerImpl;
import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.siteapi.processor.url.SiteApiConfiguration;
import io.wcm.siteapi.processor.util.impl.JsonObjectMapperImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class SiteApiRequestTest {

  private AemContext context = AppAemContext.newAemContext();

  private SiteApiConfiguration config;

  @BeforeEach
  void setUp() throws Exception {
    context.registerInjectActivateService(JsonObjectMapperImpl.class);
    context.registerInjectActivateService(ProcessorManagerImpl.class);
    registerSiteApiServlet(context);
    this.config = context.getService(SiteApiConfiguration.class);
  }

  @Test
  void testNullRequest() {
    assertFalse(isSiteApiRequest(null, config));
  }

  @Test
  void testSiteApiRequest() {
    // selector and extension are already set in AppAemContext
    assertTrue(isSiteApiRequest(context.request(), config));
  }

  @Test
  void testModelJsonRequest() {
    context.requestPathInfo().setSelectorString(SLING_MODEL_SELECTOR);
    context.requestPathInfo().setExtension(SLING_MODEL_EXTENSION);
    assertTrue(isSiteApiRequest(context.request(), config));
  }

  @Test
  void testNoSelectorSuffix() {
    context.requestPathInfo().setSelectorString(null);
    context.requestPathInfo().setExtension(null);
    assertFalse(isSiteApiRequest(context.request(), config));
  }

  @Test
  void testOtherSelector() {
    context.requestPathInfo().setSelectorString("selector1");
    assertFalse(isSiteApiRequest(context.request(), config));
  }

  @Test
  void testOtherExtension() {
    context.requestPathInfo().setExtension("ext1");
    assertFalse(isSiteApiRequest(context.request(), config));
  }

}
