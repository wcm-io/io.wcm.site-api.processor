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

import static com.adobe.cq.export.json.ExporterConstants.SLING_MODEL_EXTENSION;
import static com.adobe.cq.export.json.ExporterConstants.SLING_MODEL_SELECTOR;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.siteapi.processor.JsonObjectProcessor;
import io.wcm.siteapi.processor.Processor;
import io.wcm.siteapi.processor.ProcessorRequestContext;
import io.wcm.siteapi.processor.util.JsonObjectMapper;

/**
 * Filtered view of page content.
 * Generates subset of "model.json" version view of the page.
 *
 * <p>
 * PLEASE NOTE: This implementation requires Sling Engine 2.11 or higher (which is not part of AEM 6.5 atm).
 * Otherwise an error "SlingHttpServletResponse not of correct type" is thrown, see SLING-11569.
 * </p>
 */
@Designate(ocd = FilteringContentProcessor.Config.class, factory = true)
@Component(service = Processor.class, configurationPolicy = ConfigurationPolicy.REQUIRE, property = {
    "webconsole.configurationFactory.nameHint={suffix}"
})
@ServiceRanking(-500)
public class FilteringContentProcessor implements JsonObjectProcessor<Map<String, Object>> {

  @ObjectClassDefinition(
      name = "wcm.io Site API Filtering Content Processor",
      description = "Provides a filtered view of model.json content of a page. This implementation requires Sling Engine 2.11 or higher.")
  @interface Config {

    @AttributeDefinition(
        name = "Suffix",
        description = "Suffix for this processor.",
        required = true)
    String suffix();

    @AttributeDefinition(
        name = "Include Paths",
        description = "List of paths to include in the response. Paths are absolute paths relative to the /jcr:content node of the page.")
    String[] includePaths() default {};

    @AttributeDefinition(
        name = "Exclude Paths",
        description = "List of paths to exclude from the response. Paths are absolute paths relative to the /jcr:content node of the page.")
    String[] excludePaths() default {};

  }

  @Reference
  private JsonObjectMapper jsonObjectMapper;

  private ModelJsonPathFilter modelJsonPathFilter;
  private final Logger log = LoggerFactory.getLogger(FilteringContentProcessor.class);

  @Activate
  private void activate(Config config) {
    this.modelJsonPathFilter = new ModelJsonPathFilter(config.includePaths(), config.excludePaths());
  }

  @Override
  public @Nullable Map<String, Object> process(@NotNull ProcessorRequestContext context) {
    String modelJsonUri = context.getPage().getPath() + "." + SLING_MODEL_SELECTOR + "." + SLING_MODEL_EXTENSION;
    try {
      // get model.json output for current page
      String modelJson = getRequestOutput(context.getRequest(), modelJsonUri);
      if (modelJson == null) {
        return null;
      }
      // filter JSON
      Map<String, Object> json = jsonObjectMapper.parseToMap(modelJson);
      json = modelJsonPathFilter.filter(json);
      return json;
    }
    catch (IOException | ServletException ex) {
      log.error("Unable to get content from {}", modelJsonUri, ex);
      return null;
    }
  }

  /**
   * Gets request output from given Sling URI using request dispatcher and a synthetic response.
   * Assumes returned content is UTF-8.
   * @param request Request
   * @param uri URI to call
   * @return String output of request or null if invalid
   */
  private @Nullable String getRequestOutput(SlingHttpServletRequest request, String uri) throws IOException, ServletException {
    /*
     * use dynamic reflection proxy to capture response output.
     * it would be nicer to use https://sling.apache.org/apidocs/sling12/org/apache/sling/api/request/builder/Builders.html
     * but this is only available in Sling API 2.24, and AEM 6.5 ships with Sling API 2.22.
     */
    try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
        OutputStreamWriter writer = new OutputStreamWriter(bos, StandardCharsets.UTF_8);
        PrintWriter printWriter = new PrintWriter(writer)) {
      SlingHttpServletResponse responseProxy = (SlingHttpServletResponse)Proxy.newProxyInstance(
          SlingHttpServletResponse.class.getClassLoader(),
          new Class[] { SlingHttpServletResponse.class },
          (proxy, method, methodArgs) -> {
            if (StringUtils.equals(method.getName(), "getWriter")) {
              return printWriter;
            }
            return null;
          });

      RequestDispatcher requestDispatcher = request.getRequestDispatcher(uri);
      if (requestDispatcher != null) {
        requestDispatcher.include(request, responseProxy);
        printWriter.flush();
        return bos.toString(StandardCharsets.UTF_8);
      }
      else {
        return null;
      }
    }
  }

}
