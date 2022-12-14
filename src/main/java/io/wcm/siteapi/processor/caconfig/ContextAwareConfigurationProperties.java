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

import org.osgi.annotation.versioning.ProviderType;

/**
 * Properties that can be configured of context-aware configuration definition.
 */
@ProviderType
public final class ContextAwareConfigurationProperties {

  private ContextAwareConfigurationProperties() {
    // constants only
  }

  /**
   * Hides a property in the JSON Export of the "config" processor.
   */
  public static final String PROPERTY_HIDDEN = "siteapi:hidden";

  /**
   * Marks a string property to contain JSON data. If set to true, the string value is converted
   * to JSON and included in the JSON export. If this fails, the value is ignored.
   */
  public static final String PROPERTY_JSON_RAW_VALUE = "siteapi:jsonRawValue";

  /**
   * Marks a context-aware configuration to be embedded into the main JSON response of the "config" processor.
   * This behavior is enabled by default, you can disabled it by setting it to false. In this case,
   * the main response contains a link to a separate URL of the configuration.
   */
  public static final String PROPERTY_CONFIG_EMBEDDED = "siteapi:embedded";

}
