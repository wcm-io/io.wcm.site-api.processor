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
package io.wcm.siteapi.processor.impl.content;

import static io.wcm.siteapi.processor.ProcessorConstants.PROCESSOR_CONTENT;
import static io.wcm.siteapi.processor.textcontext.SiteApiTestUtil.processorRequestContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import javax.servlet.RequestDispatcher;

import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockRequestDispatcherFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.wcm.siteapi.processor.ProcessorRequestContext;
import io.wcm.siteapi.processor.SlingHttpServletProcessor;
import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class ContentProcessorTest {

  private AemContext context = AppAemContext.newAemContext();

  private SlingHttpServletProcessor underTest;

  @Mock
  private RequestDispatcher requestDispatcher;
  private String requestDispatcherPath;

  @BeforeEach
  void setUp() {
    underTest = context.registerInjectActivateService(ContentProcessor.class);
    context.currentPage(context.create().page("/content/test"));

    context.request().setRequestDispatcherFactory(new MockRequestDispatcherFactory() {
      @Override
      public RequestDispatcher getRequestDispatcher(Resource resource, RequestDispatcherOptions options) {
        throw new UnsupportedOperationException();
      }
      @Override
      public RequestDispatcher getRequestDispatcher(String path, RequestDispatcherOptions options) {
        requestDispatcherPath = path;
        return requestDispatcher;
      }
    });
  }

  @Test
  void testProcessor() throws Exception {
    ProcessorRequestContext processorRequestContext = processorRequestContext(context.request(), PROCESSOR_CONTENT);
    underTest.process(processorRequestContext, context.response());

    assertEquals("/content/test.model.json", requestDispatcherPath);
    verify(requestDispatcher, times(1)).forward(context.request(), context.response());
  }

}
