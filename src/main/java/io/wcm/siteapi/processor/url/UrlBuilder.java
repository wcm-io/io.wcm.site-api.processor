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
package io.wcm.siteapi.processor.url;

import org.apache.sling.api.SlingHttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

import com.day.cq.wcm.api.Page;

/**
 * Builds URLs for Site API calls.
 */
@ConsumerType
public interface UrlBuilder {

  /**
   * Build Site API URL.
   * @param page Page
   * @param suffix Suffix
   * @param suffixExtension Suffix extension
   * @param request Request
   * @return URL
   */
  String build(@NotNull Page page, @NotNull String suffix, @Nullable String suffixExtension,
      @NotNull SlingHttpServletRequest request);

}
