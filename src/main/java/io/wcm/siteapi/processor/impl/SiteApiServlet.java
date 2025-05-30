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

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

import java.io.IOException;

import javax.servlet.Servlet;
import javax.servlet.ServletException;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.HttpConstants;
import org.apache.sling.api.servlets.SlingSafeMethodsServlet;
import org.apache.sling.servlets.annotations.SlingServletResourceTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.NameConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.PageManagerFactory;
import com.day.cq.wcm.api.WCMMode;

import io.wcm.siteapi.processor.JsonObjectProcessor;
import io.wcm.siteapi.processor.Processor;
import io.wcm.siteapi.processor.ProcessorManager;
import io.wcm.siteapi.processor.ProcessorRequestContext;
import io.wcm.siteapi.processor.SlingHttpServletProcessor;
import io.wcm.siteapi.processor.url.JsonSuffix;
import io.wcm.siteapi.processor.url.SiteApiConfiguration;
import io.wcm.siteapi.processor.util.JsonObjectMapper;

/**
 * Accepts all Site API calls and redirects processing to a {@link SlingHttpServletProcessor} based on the suffix.
 */
@Designate(ocd = SiteApiServlet.Config.class)
@Component(service = { Servlet.class, SiteApiConfiguration.class })
@SlingServletResourceTypes(
    resourceTypes = NameConstants.NT_PAGE,
    methods = HttpConstants.METHOD_GET)
@SuppressWarnings("squid:S1948") // servlet is not serialized
public class SiteApiServlet extends SlingSafeMethodsServlet implements SiteApiConfiguration {

  @ObjectClassDefinition(
      name = "wcm.io Site API Servlet",
      description = "Configures the request mapping of Site API Servlet.")
  @SuppressWarnings("java:S100")
  @interface Config {

    @AttributeDefinition(
        name = "Selector+Version",
        description = "Define Sling selector for matching Site API request for current version with syntax '<selector>.<version>'.")
    String sling_servlet_selectors() default "site";

    @AttributeDefinition(
        name = "Extension",
        description = "Extension that is used for servlet.")
    String sling_servlet_extensions() default "api";

  }

  static final String JSON_CONTENT_TYPE = "application/json;charset=UTF-8";

  private static final long serialVersionUID = 1L;
  private static final Logger log = LoggerFactory.getLogger(SiteApiServlet.class);

  @Reference
  private PageManagerFactory pageManagerFactory;
  @Reference
  private ProcessorManager processorManager;
  @Reference
  private JsonObjectMapper jsonObjectMapper;

  private String selector;
  private String extension;

  @Activate
  private void activate(Config config) {
    this.selector = config.sling_servlet_selectors();
    this.extension = config.sling_servlet_extensions();
  }


  @Override
  protected void doGet(@NotNull SlingHttpServletRequest request, @NotNull SlingHttpServletResponse response)
      throws ServletException, IOException {
    Resource resource = request.getResource();

    // force disabled mode for proper media/link handling
    WCMMode.DISABLED.toRequest(request);

    // ensure selector matches exactly (no additional selectors allowed)
    if (!StringUtils.equals(request.getRequestPathInfo().getSelectorString(), this.selector)) {
      response.sendError(SC_NOT_FOUND);
      return;
    }

    // get processor matching given suffix
    Processor processor = null;
    JsonSuffix suffix = JsonSuffix.parse(request.getRequestPathInfo().getSuffix());
    if (suffix != null) {
      processor = processorManager.getMatching(suffix.getSuffix(), resource);
    }
    if (suffix == null || processor == null) {
      response.sendError(SC_NOT_FOUND);
      return;
    }

    // ensure valid page
    PageManager pageManager = pageManagerFactory.getPageManager(request.getResourceResolver());
    Page currentPage = getCurrentPage(resource, pageManager);
    if (currentPage == null || !ensurePageHasContent(currentPage)) {
      response.sendError(SC_NOT_FOUND);
      return;
    }

    // handle request using processor
    ProcessorRequestContext context = new ProcessorRequestContextImpl(suffix.getSuffix(), suffix.getSuffixExtension(),
        request, pageManager, currentPage);
    if (processor instanceof JsonObjectProcessor) {
      processJsonObject((JsonObjectProcessor)processor, context, response);
    }
    else if (processor instanceof SlingHttpServletProcessor) {
      ((SlingHttpServletProcessor)processor).process(context, response);
    }
    else {
      throw new ServletException("Processor does not implement JsonObjectProcessor or SlingHttpServletResponse: " + processor);
    }
  }

  private void processJsonObject(JsonObjectProcessor<?> processor, ProcessorRequestContext context,
      SlingHttpServletResponse response) throws IOException {
    Object result = processor.process(context);
    if (result == null) {
      response.sendError(SC_NOT_FOUND);
    }
    else {
      response.setContentType(JSON_CONTENT_TYPE);
      String jsonString = jsonObjectMapper.toJsonString(result);
      response.getWriter().write(jsonString);
    }
  }

  private @Nullable Page getCurrentPage(@NotNull Resource resource, @NotNull PageManager pageManager) {
    Page page = pageManager.getContainingPage(resource);
    if (page == null) {
      log.debug("No page found for given resource: {}", resource.getPath());
    }
    return page;
  }

  private boolean ensurePageHasContent(@NotNull Page page) {
    boolean hasContent = page.hasContent();
    if (!hasContent) {
      log.debug("Ignoring page without jcr:content node: {}", page.getPath());
    }
    return hasContent;
  }

  @Override
  public @NotNull String getSelector() {
    return selector;
  }

  @Override
  public @NotNull String getExtension() {
    return extension;
  }

  @Override
  public @NotNull String getContextPath() {
    return getServletContext().getContextPath();
  }

}
