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

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestPathInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

import io.wcm.siteapi.processor.url.SiteApiConfiguration;

/**
 * Checks if a given servlet request is a request within an Site API processor.
 */
@ProviderType
public final class SiteApiRequest {

  private SiteApiRequest() {
    // static methods only
  }

  /**
   * Check if current request is a Site API request (has matching selector and extension).
   * Request to .model.json is treated the same way (used internally by content processor).
   * @param request Request
   * @param siteApiConfiguration Site API configuration
   * @return true if Site API request
   */
  public static boolean isSiteApiRequest(@Nullable SlingHttpServletRequest request,
      @NotNull SiteApiConfiguration siteApiConfiguration) {
    if (request == null) {
      return false;
    }
    RequestPathInfo requestPathInfo = request.getRequestPathInfo();
    String selector = requestPathInfo.getSelectorString();
    String extension = requestPathInfo.getExtension();
    return isSiteApiRequest(selector, extension, siteApiConfiguration)
        || isModelJsonRequest(selector, extension);
  }

  private static boolean isSiteApiRequest(@Nullable String selector, @Nullable String extension,
      @NotNull SiteApiConfiguration siteApiConfiguration) {
    return StringUtils.equals(selector, siteApiConfiguration.getSelector())
        && StringUtils.equals(extension, siteApiConfiguration.getExtension());
  }

  private static boolean isModelJsonRequest(@Nullable String selector, @Nullable String extension) {
    return StringUtils.equals(selector, SLING_MODEL_SELECTOR)
        && StringUtils.equals(extension, SLING_MODEL_EXTENSION);
  }

}
