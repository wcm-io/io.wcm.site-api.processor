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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Converts context-aware configuration to a nested Map structure which can be serialized to JSON.
 * Mappings of individual properties marked with certain metadata can be customized by implementing
 * {@link ContextAwareConfigurationPropertyMapper} services.
 */
@ProviderType
public interface ContextAwareConfigurationMapper {

  /**
   * Gets a representation of the context-aware configuration consisting of nested Maps and Lists.
   * @param configName Configuration name
   * @param request Request with context resource
   * @return Representation of configuration or null if metadata or configuration is not present or invalid
   */
  @Nullable
  Object get(@NotNull String configName, @NotNull SlingHttpServletRequest request);

}
