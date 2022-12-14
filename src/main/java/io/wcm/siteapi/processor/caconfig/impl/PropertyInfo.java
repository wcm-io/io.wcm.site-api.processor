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

import static io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationProperties.PROPERTY_HIDDEN;
import static io.wcm.siteapi.processor.caconfig.impl.ConfigurationMetadataUtil.PROPERTY_REQUIRED;

import org.apache.sling.caconfig.management.ValueInfo;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

class PropertyInfo {

  private final String name;
  private final Object value;
  private final PropertyMetadata<?> metadata;

  PropertyInfo(@NotNull ValueInfo<?> valueInfo) {
    this.name = valueInfo.getName();
    this.value = valueInfo.getEffectiveValue();
    this.metadata = valueInfo.getPropertyMetadata();
  }

  public @NotNull String getName() {
    return this.name;
  }

  public @Nullable Object getValue() {
    return this.value;
  }

  @SuppressWarnings("java:S1452")
  public @Nullable PropertyMetadata<?> getMetadata() {
    return this.metadata;
  }

  boolean isHidden() {
    return ConfigurationMetadataUtil.isBoolean(metadata, PROPERTY_HIDDEN, false);
  }

  boolean isRequired() {
    return ConfigurationMetadataUtil.isBoolean(metadata, PROPERTY_REQUIRED, false);
  }

  boolean isNestedConfiguration() {
    return this.metadata != null && this.metadata.isNestedConfiguration();
  }

}
