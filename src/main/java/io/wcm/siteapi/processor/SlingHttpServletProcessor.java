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

import java.io.IOException;

import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Processes Site API requests. Directly writes output to {@link SlingHttpServletResponse};
 * <p>
 * To be implemented as OSGi service registered to {@link Processor} with mandatory
 * property {@link ProcessorConstants#PROPERTY_SUFFIX} to find the processor matching the current request.
 * </p>
 * <p>
 * This is a "lowlevel" interface if you need full control about request and response processes.
 * Most processors should extends the "high-level" {@link JsonObjectProcessor} class instead.
 * </p>
 */
@ConsumerType
public interface SlingHttpServletProcessor extends Processor {

  /**
   * Process request.
   * @param context Request context objects
   * @param response Sling response
   * @throws ServletException Servlet exception
   * @throws IOException I/O exception
   */
  void process(@NotNull ProcessorRequestContext context, @NotNull SlingHttpServletResponse response)
      throws ServletException, IOException;

}
