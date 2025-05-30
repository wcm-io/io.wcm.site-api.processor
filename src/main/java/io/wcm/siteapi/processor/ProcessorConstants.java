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

import org.osgi.annotation.versioning.ProviderType;

/**
 * Properties and suffix names for Site API processors.
 */
@ProviderType
public final class ProcessorConstants {

  /**
   * Defines the suffix that is used to build the URL (without '.json' extension).
   * OSGi service property. This property is mandatory.
   */
  public static final String PROPERTY_SUFFIX = "suffix";

  /**
   * Defining a regular pattern to match the URL suffix (without '.json' extension).
   * If not set, an exact match with the suffix is enforced.
   * This allows processors to produce "variable" output based on additional suffix information
   * passed after the main suffix part.
   * OSGi service property.
   */
  public static final String PROPERTY_SUFFIX_PATTERN = "suffixPattern";

  /**
   * A processor is enabled by default. If an "enabled" property exists, and is set to false, the processor is disabled.
   * OSGi service property.
   */
  public static final String PROPERTY_ENABLED = "enabled";

  /**
   * Suffix of the built-in index processor.
   */
  public static final String PROCESSOR_INDEX = "index";

  /**
   * Suffix of the built-in content processor. Forwards to the output of .model.json output.
   */
  public static final String PROCESSOR_CONTENT = "content";

  /**
   * Suffix of the built-in context-aware configuration processor.
   */
  public static final String PROCESSOR_CONFIG = "config";

  private ProcessorConstants() {
    // constants only
  }

}
