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

import java.util.Collection;

import org.jetbrains.annotations.NotNull;
import org.osgi.annotation.versioning.ConsumerType;

import io.wcm.sling.commons.caservice.ContextAwareService;

/**
 * Configures context-aware configuration names/classes to be exposed via Site API.
 * <p>
 * This can either be implemented as OSGI service, or configured via OSGI configuration
 * by using {@link io.wcm.siteapi.processor.caconfig.impl.ContextAwareConfigurationExportImpl}.
 * </p>
 */
@ConsumerType
public interface ContextAwareConfigurationExport extends ContextAwareService {

  /**
   * @return List context-aware configuration names/classes.
   */
  @NotNull
  Collection<String> getNames();

}
