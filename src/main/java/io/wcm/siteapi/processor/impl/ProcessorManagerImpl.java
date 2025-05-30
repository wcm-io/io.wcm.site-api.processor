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
package io.wcm.siteapi.processor.impl;

import java.util.Collections;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.FieldOption;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

import io.wcm.siteapi.processor.Processor;
import io.wcm.siteapi.processor.ProcessorManager;
import io.wcm.siteapi.processor.ProcessorMetadata;
import io.wcm.siteapi.processor.SlingHttpServletProcessor;
import io.wcm.sling.commons.caservice.ContextAwareServiceCollectionResolver;
import io.wcm.sling.commons.caservice.ContextAwareServiceResolver;

/**
 * Collects all {@link SlingHttpServletProcessor} services and allows to get the matching processor for a given suffix.
 */
@Component(service = ProcessorManager.class)
public class ProcessorManagerImpl implements ProcessorManager {

  @Reference
  private ContextAwareServiceResolver serviceResolver;

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, fieldOption = FieldOption.UPDATE,
      policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
  private SortedSet<ServiceReference<Processor>> processors = new ConcurrentSkipListSet<>(Collections.reverseOrder());

  private ContextAwareServiceCollectionResolver<Processor, ProcessorData> serviceCollectionResolver;

  @Activate
  private void activate() {
    this.serviceCollectionResolver = serviceResolver.getCollectionResolver(
        this.processors, ProcessorData::new);
  }

  @Deactivate
  private void deactivate() {
    this.serviceCollectionResolver.close();
  }

  /**
   * Get processor matching for suffix.
   * @param suffix Suffix
   * @return Processor or null of no matching was found
   */
  @Override
  public @Nullable Processor getMatching(@NotNull String suffix, @NotNull Resource contextResource) {
    return serviceCollectionResolver.resolveAllDecorated(contextResource)
        .filter(ProcessorData::isValid)
        .filter(processor -> processor.matches(suffix))
        .map(ProcessorData::getProcessor)
        .findFirst().orElse(null);
  }

  @Override
  @SuppressWarnings("null")
  public @NotNull Stream<ProcessorMetadata> getAll(@NotNull Resource contextResource) {
    return serviceCollectionResolver.resolveAllDecorated(contextResource)
        .filter(ProcessorData::isValid)
        .map(ProcessorMetadata.class::cast);
  }

}
