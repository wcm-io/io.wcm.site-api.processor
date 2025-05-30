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
package io.wcm.siteapi.processor.url.impl;

import static io.wcm.siteapi.processor.textcontext.SiteApiTestUtil.registerSiteApiServlet;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.day.cq.wcm.api.Page;

import io.wcm.siteapi.processor.impl.ProcessorManagerImpl;
import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.siteapi.processor.url.UrlBuilder;
import io.wcm.siteapi.processor.util.impl.JsonObjectMapperImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class SimpleUrlBuilderTest {

  private AemContext context = AppAemContext.newAemContext();

  private UrlBuilder underTest;
  private Page page;

  @BeforeEach
  protected void setUp() {
    context.registerInjectActivateService(JsonObjectMapperImpl.class);
    context.registerInjectActivateService(ProcessorManagerImpl.class);
    registerSiteApiServlet(context);

    underTest = context.registerInjectActivateService(SimpleUrlBuilder.class);
    page = context.create().page("/content/test");
  }

  @Test
  void testUrl() {
    assertEquals("/content/test.site.api/suffix1.json", underTest.build(page, "suffix1", null, context.request()));
    assertEquals("/content/test.site.api/suffix1/sub1.json", underTest.build(page, "suffix1", "sub1", context.request()));
  }

}
