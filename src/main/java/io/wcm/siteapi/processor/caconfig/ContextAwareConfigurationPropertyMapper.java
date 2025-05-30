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
package io.wcm.siteapi.processor.caconfig;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.sling.commons.caservice.ContextAwareService;

/**
 * Defines custom mappings of context-aware configuration property values to a re-mapped target value
 * or target structure (e.g. with nested maps).
 * @param <T> Target object type
 */
@ConsumerType
public interface ContextAwareConfigurationPropertyMapper<T> extends ContextAwareService {

  /**
   * Checks if this mapper accepts the given property.
   * @param value Source value from context-aware configuration.
   * @param metadata Property metadata
   * @param request Request with context resource
   * @return true if the mapper accepts this property
   */
  boolean accept(@NotNull Object value, @NotNull PropertyMetadata<?> metadata, @NotNull SlingHttpServletRequest request);

  /**
   * Maps a property value.
   * @param value Source value from context-aware configuration.
   * @param metadata Property metadata
   * @param request Request with context resource
   * @return Mapped value or null if the value is invalid
   */
  @Nullable
  T map(@NotNull Object value, @NotNull PropertyMetadata<?> metadata, @NotNull SlingHttpServletRequest request);

}
