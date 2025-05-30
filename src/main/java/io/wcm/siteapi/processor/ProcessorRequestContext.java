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
package io.wcm.siteapi.processor;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import io.wcm.siteapi.processor.url.JsonSuffix;

/**
 * Context objects for API request processing.
 */
@ProviderType
public interface ProcessorRequestContext {

  /**
   * @return Suffix (see {@link JsonSuffix})
   */
  @NotNull
  String getSuffix();

  /**
   * @return Suffix extension (see {@link JsonSuffix})
   */
  @Nullable
  String getSuffixExtension();

  /**
   * @return Sling request
   */
  @NotNull
  SlingHttpServletRequest getRequest();

  /**
   * @return Page manager
   */
  @NotNull
  PageManager getPageManager();

  /**
   * @return Current resource
   */
  @NotNull
  Resource getResource();

  /**
   * @return Current page
   */
  @NotNull
  Page getPage();

}
