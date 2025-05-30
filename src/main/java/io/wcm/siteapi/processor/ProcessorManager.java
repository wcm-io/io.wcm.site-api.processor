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

import java.util.stream.Stream;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Manages all {@link JsonObjectProcessor} and {@link SlingHttpServletProcessor} services.
 * The matching processors are detected based on suffix and context path.
 */
@ProviderType
public interface ProcessorManager {

  /**
   * Get processor matching for suffix and context.
   * @param suffix Suffix
   * @param contextResource Context resource to fetch the matching context-aware processor services implementation.
   * @return Processor or null of no matching was found.
   *         The processor implements either {@link JsonObjectProcessor} or {@link SlingHttpServletProcessor}.
   */
  @Nullable
  Processor getMatching(@NotNull String suffix, @NotNull Resource contextResource);

  /**
   * Get all registered processor for this context.
   * @param contextResource Context resource to fetch the related context-aware processor services implementation.
   * @return List of all processors
   */
  @NotNull
  Stream<ProcessorMetadata> getAll(@NotNull Resource contextResource);

}
