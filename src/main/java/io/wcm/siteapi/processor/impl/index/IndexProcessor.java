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
package io.wcm.siteapi.processor.impl.index;

import static io.wcm.siteapi.processor.ProcessorConstants.PROCESSOR_INDEX;
import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_SUFFIX;

import java.util.Collection;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import com.day.cq.wcm.api.Page;

import io.wcm.siteapi.processor.JsonObjectProcessor;
import io.wcm.siteapi.processor.Processor;
import io.wcm.siteapi.processor.ProcessorManager;
import io.wcm.siteapi.processor.ProcessorMetadata;
import io.wcm.siteapi.processor.ProcessorRequestContext;
import io.wcm.siteapi.processor.url.UrlBuilder;

/**
 * Index processor
 */
@Designate(ocd = IndexProcessor.Config.class)
@Component(service = Processor.class, configurationPolicy = ConfigurationPolicy.REQUIRE,
    property = PROPERTY_SUFFIX + "=" + PROCESSOR_INDEX)
@ServiceRanking(-500)
public class IndexProcessor implements JsonObjectProcessor<Collection<ProcessorIndex>> {

  @ObjectClassDefinition(
      name = "wcm.io Site API Index Processor",
      description = "Provides an index of processors.")
  @interface Config {

    @AttributeDefinition(
        name = "Enabled",
        description = "Processor is enabled.")
    boolean enabled() default false;

  }

  @Reference
  private ProcessorManager processorManager;
  @Reference
  private UrlBuilder urlBuilder;

  @Override
  public @Nullable Collection<ProcessorIndex> process(@NotNull ProcessorRequestContext context) {
    return getIndex(context.getPage(), context.getRequest());
  }

  @SuppressWarnings("null")
  private Collection<ProcessorIndex> getIndex(@NotNull Page page, @NotNull SlingHttpServletRequest request) {
    return processorManager.getAll(page.getContentResource())
        .map(ProcessorMetadata::getSuffix)
        .filter(suffix -> !StringUtils.equals(suffix, PROCESSOR_INDEX))
        .map(suffix -> new ProcessorIndex(suffix, urlBuilder.build(page, suffix, null, request)))
        .collect(Collectors.toList());
  }

}
