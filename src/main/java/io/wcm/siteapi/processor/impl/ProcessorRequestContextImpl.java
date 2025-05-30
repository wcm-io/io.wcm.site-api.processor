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

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import io.wcm.siteapi.processor.ProcessorRequestContext;

/**
 * Context objects for API request processing.
 */
public final class ProcessorRequestContextImpl implements ProcessorRequestContext {

  private final String suffix;
  private final String suffixExtension;
  private final SlingHttpServletRequest request;
  private final PageManager pageManager;
  private final Page currentPage;

  /**
   * @param suffix Suffix string (without '.json' extension)
   * @param request Sling request
   * @param pageManager Page manager
   * @param currentPage Current page
   */
  public ProcessorRequestContextImpl(@NotNull String suffix, @Nullable String suffixExtension,
      @NotNull SlingHttpServletRequest request,
      @NotNull PageManager pageManager, @NotNull Page currentPage) {
    this.suffix = suffix;
    this.suffixExtension = suffixExtension;
    this.request = request;
    this.pageManager = pageManager;
    this.currentPage = currentPage;
  }

  @Override
  public @NotNull String getSuffix() {
    return this.suffix;
  }

  @Override
  public @Nullable String getSuffixExtension() {
    return this.suffixExtension;
  }

  @Override
  public @NotNull SlingHttpServletRequest getRequest() {
    return this.request;
  }

  @Override
  public @NotNull PageManager getPageManager() {
    return this.pageManager;
  }

  @Override
  public @NotNull Resource getResource() {
    return this.request.getResource();
  }

  @Override
  public @NotNull Page getPage() {
    return this.currentPage;
  }

}
