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
package io.wcm.siteapi.processor.impl.caconfig;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Configures a list of context-aware configuration names/classes to be exposed via Site API.
 */
@Designate(ocd = ContextAwareConfigurationExport.Config.class, factory = true)
@Component(service = ContextAwareConfigurationExport.class, property = {
    "webconsole.configurationFactory.nameHint={names}"
})
public final class ContextAwareConfigurationExport {

  @ObjectClassDefinition(
      name = "wcm.io Site API Context-Aware Configuration Export",
      description = "Configures a list of context-aware configuration names/classes to be exposed via Site API.")
  @interface Config {

    @AttributeDefinition(
        name = "Config Names",
        description = "List context-aware configuration names/classes.")
    String[] names() default {};

  }

  private Collection<String> names;

  @Activate
  private void activate(Config config) {
    this.names = Arrays.asList(config.names());
  }

  /**
   * @return List context-aware configuration names/classes.
   */
  public Collection<String> getNames() {
    return Collections.unmodifiableCollection(names);
  }

}
