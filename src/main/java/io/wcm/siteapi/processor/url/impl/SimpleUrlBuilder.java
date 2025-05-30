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

import org.apache.sling.api.SlingHttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceRanking;

import com.day.cq.wcm.api.Page;

import io.wcm.siteapi.processor.url.JsonSuffix;
import io.wcm.siteapi.processor.url.SiteApiConfiguration;
import io.wcm.siteapi.processor.url.UrlBuilder;

/**
 * Simple builder for Site API URLs without externalization or URL handling.
 */
@Component(service = UrlBuilder.class)
@ServiceRanking(-500)
public class SimpleUrlBuilder implements UrlBuilder {

  @Reference
  private SiteApiConfiguration config;

  @Override
  public String build(@NotNull Page page, @NotNull String suffix, @Nullable String suffixExtension,
      @NotNull SlingHttpServletRequest request) {
    return new StringBuilder()
        .append(config.getContextPath())
        .append(page.getPath())
        .append(".")
        .append(config.getSelector())
        .append(".")
        .append(config.getExtension())
        .append(JsonSuffix.build(suffix, suffixExtension))
        .toString();
  }

}
