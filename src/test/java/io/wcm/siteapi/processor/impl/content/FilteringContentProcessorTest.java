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

import static io.wcm.siteapi.processor.ProcessorConstants.PROCESSOR_CONTENT;
import static io.wcm.siteapi.processor.impl.content.JsonTestUtils.assertNoPath;
import static io.wcm.siteapi.processor.impl.content.JsonTestUtils.assertPath;
import static io.wcm.siteapi.processor.impl.content.JsonTestUtils.loadJsonString;
import static io.wcm.siteapi.processor.textcontext.SiteApiTestUtil.processorRequestContext;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;

import java.util.List;
import java.util.Map;

import javax.servlet.RequestDispatcher;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.request.RequestDispatcherOptions;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.testing.mock.sling.servlet.MockRequestDispatcherFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import io.wcm.siteapi.processor.JsonObjectProcessor;
import io.wcm.siteapi.processor.ProcessorRequestContext;
import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.siteapi.processor.util.JsonObjectMapper;
import io.wcm.siteapi.processor.util.impl.JsonObjectMapperImpl;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
@ExtendWith(MockitoExtension.class)
class FilteringContentProcessorTest {

  private AemContext context = AppAemContext.newAemContext();

  private JsonObjectProcessor<Map<String, Object>> underTest;

  @Mock
  private RequestDispatcher requestDispatcher;
  private String requestDispatcherPath;
  private JsonObjectMapper jsonObjectMapper;

  @BeforeEach
  void setUp() {
    jsonObjectMapper = context.registerInjectActivateService(JsonObjectMapperImpl.class);

    underTest = context.registerInjectActivateService(FilteringContentProcessor.class,
        "suffix", "contentFiltering",
        "includePaths", List.of("/root"),
        "excludePaths", List.of("/root/header", "/root/footer"));
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
  @SuppressWarnings("null")
  void testProcessor() throws Exception {

    // simulate model.json from JSON file
    doAnswer(new Answer() {
      @Override
      public Object answer(InvocationOnMock invocation) throws Throwable {
        SlingHttpServletResponse response = invocation.getArgument(1);
        String modelJson = loadJsonString("sample-model.json");
        response.getWriter().print(modelJson);
        return null;
      }
    }).when(requestDispatcher).include(any(SlingHttpServletRequest.class), any(SlingHttpServletResponse.class));

    ProcessorRequestContext processorRequestContext = processorRequestContext(context.request(), PROCESSOR_CONTENT);
    Map<String, Object> result = underTest.process(processorRequestContext);

    assertEquals("/content/test.model.json", requestDispatcherPath);

    // assert exclusion
    DocumentContext json = JsonPath.parse(jsonObjectMapper.toJsonString(result));
    assertPath(json, "$[':items'].root[':items'].container");
    assertNoPath(json, "$[':items'].root[':items'].header");
    assertNoPath(json, "$[':items'].root[':items'].footer");
    assertEquals(List.of("container"), json.read("$[':items'].root[':itemsOrder']"), "itemsOrder");
  }

}
