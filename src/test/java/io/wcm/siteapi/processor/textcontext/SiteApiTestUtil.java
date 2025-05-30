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
package io.wcm.siteapi.processor.textcontext;

import static io.wcm.siteapi.processor.textcontext.AppAemContext.API_EXTENSION;
import static io.wcm.siteapi.processor.textcontext.AppAemContext.API_SELECTOR;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_EXTENSIONS;
import static org.apache.sling.api.servlets.ServletResolverConstants.SLING_SERVLET_SELECTORS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import io.wcm.siteapi.processor.ProcessorRequestContext;
import io.wcm.siteapi.processor.impl.ProcessorRequestContextImpl;
import io.wcm.siteapi.processor.impl.SiteApiServlet;
import io.wcm.testing.mock.aem.junit5.AemContext;

/**
 * Helper methods for Site API unit tests.
 */
public final class SiteApiTestUtil {

  private SiteApiTestUtil() {
    // static methods only
  }

  /**
   * Register and initialize SiteApiServlet.
   */
  @SuppressWarnings("null")
  public static @NotNull SiteApiServlet registerSiteApiServlet(@NotNull AemContext context) {
    SiteApiServlet servlet = context.registerInjectActivateService(SiteApiServlet.class,
        SLING_SERVLET_SELECTORS, API_SELECTOR,
        SLING_SERVLET_EXTENSIONS, API_EXTENSION);

    // set context path
    ServletContext servletContext = mock(ServletContext.class);
    lenient().when(servletContext.getContextPath()).thenReturn("");
    ServletConfig servletConfig = mock(ServletConfig.class);
    lenient().when(servletConfig.getServletContext()).thenReturn(servletContext);
    try {
      servlet.init(servletConfig);
    }
    catch (ServletException ex) {
      throw new RuntimeException(ex);
    }

    return servlet;
  }

  /**
   * Create {@link ProcessorRequestContext} instance, assuming current page is set.
   */
  public static @NotNull ProcessorRequestContext processorRequestContext(@NotNull SlingHttpServletRequest request,
      @NotNull String suffix) {
    return processorRequestContext(request, suffix, null);
  }

  /**
   * Create {@link ProcessorRequestContext} instance, assuming current page is set.
   */
  public static @NotNull ProcessorRequestContext processorRequestContext(@NotNull SlingHttpServletRequest request,
      @NotNull String suffix, @Nullable String suffixExtension) {
    PageManager pageManager = request.getResourceResolver().adaptTo(PageManager.class);
    if (pageManager == null) {
      throw new IllegalArgumentException("PageManager missing.");
    }
    Page page = pageManager.getContainingPage(request.getResource());
    if (page == null) {
      throw new IllegalArgumentException("Current page missing.");
    }
    return new ProcessorRequestContextImpl(suffix, suffixExtension, request, pageManager, page);
  }

}
