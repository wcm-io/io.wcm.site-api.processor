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
package io.wcm.siteapi.processor.caconfig.impl;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Helper methods for working with property metadata.
 */
public final class ConfigurationMetadataUtil {

  /**
   * Define this property as required/mandatory. Configuration cannot be saved if no value is given.
   * Copied from io.wcm.caconfig.editor.EditorProperties to avoid direct dependency.
   */
  public static final String PROPERTY_REQUIRED = "required";

  private ConfigurationMetadataUtil() {
    // static methods only
  }

  /**
   * Checks if a property is set to true.
   * @param metadata Property metadata
   * @param propertyName Property name
   * @param defaultValue Default value
   * @return true if value is true
   */
  public static boolean isBoolean(@Nullable PropertyMetadata<?> metadata, @NotNull String propertyName, boolean defaultValue) {
    return toBoolean(metadata != null ? metadata.getProperties().get(propertyName) : null, defaultValue);
  }

  /**
   * Checks if a property is set to true.
   * @param metadata Property metadata
   * @param propertyName Property name
   * @return true if value is true
   */
  public static boolean isBoolean(@Nullable ConfigurationMetadata metadata, @NotNull String propertyName, boolean defaultValue) {
    return toBoolean(metadata != null ? metadata.getProperties().get(propertyName) : null, defaultValue);
  }

  private static boolean toBoolean(String booleanString, boolean defaultValue) {
    if (booleanString == null) {
      return defaultValue;
    }
    else {
      return BooleanUtils.toBoolean(booleanString);
    }
  }

}
