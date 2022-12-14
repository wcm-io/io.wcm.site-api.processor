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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Processes Site API requests. Generating an Object to be serialized as JSON.
 * <p>
 * To be implemented as OSGi service registered to {@link Processor} with mandatory
 * property {@link ProcessorConstants#PROPERTY_SUFFIX} to find the processor matching the current request.
 * </p>
 */
@ConsumerType
public interface JsonObjectProcessor<T> extends Processor {

  /**
   * Generates an Object that is serialized to JSON and returned as response.
   * @param context Context objects for request processing.
   * @return Object to be serialized. If it is null, HTTP 404 is returned as response.
   */
  @Nullable
  T process(@NotNull ProcessorRequestContext context);

}
