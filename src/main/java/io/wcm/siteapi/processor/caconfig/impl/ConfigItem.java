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

import org.jetbrains.annotations.Nullable;

/**
 * Context-Aware configuration item.
 */
interface ConfigItem<T> {

  /**
   * @return true if configuration is empty. If a configuration has no configuration,
   *         but has default values or overridden values, it is not considered empty.
   */
  boolean isEmpty();

  /**
   * @return true if config is valid (all required properties present)
   */
  boolean isValid();

  /**
   * @return Returns object that can be serialized to JSON. Returns null if empty or invalid.
   */
  @Nullable
  T toJsonObject();

}
