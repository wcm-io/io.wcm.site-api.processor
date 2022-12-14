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
package io.wcm.siteapi.processor.caconfig.impl.sample;

import static io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationProperties.PROPERTY_CONFIG_EMBEDDED;

import org.apache.sling.caconfig.annotation.Configuration;
import org.apache.sling.caconfig.annotation.Property;

/**
 * Context-aware configuration example with link instead embed configuration.
 */
@Configuration(property = {
    PROPERTY_CONFIG_EMBEDDED + "=false"
})
public @interface ConfigSampleNotEmbedded {

  /**
   * @return String parameter
   */
  @Property
  String stringParam();

}
