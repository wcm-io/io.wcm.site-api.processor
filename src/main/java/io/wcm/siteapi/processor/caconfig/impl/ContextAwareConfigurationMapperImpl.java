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

import static java.util.function.Predicate.not;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.SortedSet;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.caconfig.management.ConfigurationCollectionData;
import org.apache.sling.caconfig.management.ConfigurationData;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
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

import io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationMapper;
import io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationPropertyMapper;
import io.wcm.sling.commons.caservice.ContextAwareServiceCollectionResolver;
import io.wcm.sling.commons.caservice.ContextAwareServiceResolver;

/**
 * Implements {@link ContextAwareConfigurationMapper},
 */
@Component(service = ContextAwareConfigurationMapper.class)
public class ContextAwareConfigurationMapperImpl implements ContextAwareConfigurationMapper {

  // ignore property names with namespaces sling/jcr/cq
  private static final Pattern IGNORED_SYSTEM_PROPERTY_NAMES = Pattern.compile("^(sling|jcr|cq):.*$");

  @Reference
  private ConfigurationManager configManager;

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, fieldOption = FieldOption.UPDATE,
      policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
  private SortedSet<ServiceReference<ContextAwareConfigurationPropertyMapper<Object>>> propertyMapper = new ConcurrentSkipListSet<>(
      Collections.reverseOrder());

  @Reference
  private ContextAwareServiceResolver serviceResolver;
  private ContextAwareServiceCollectionResolver<ContextAwareConfigurationPropertyMapper<Object>, Void> propertyMapperResolver;

  @Activate
  private void activate() {
    this.propertyMapperResolver = serviceResolver.getCollectionResolver(this.propertyMapper);
  }

  @Deactivate
  private void deactivate() {
    this.propertyMapperResolver.close();
  }


  @Override
  public @Nullable Object get(@NotNull String configName, @NotNull SlingHttpServletRequest request) {
    ConfigurationMetadata metadata = configManager.getConfigurationMetadata(configName);
    if (metadata != null) {
      return build(metadata, request);
    }
    return null;
  }

  /**
   * Build JSON representation of context-aware configuration.
   * @param metadata Configuration Metadata
   * @return Map/List with configuration data, or null if configuration or metadata is not present.
   */
  @Nullable
  private Object build(@NotNull ConfigurationMetadata metadata, @NotNull SlingHttpServletRequest request) {
    Resource contextResource = request.getResource();

    // get property mappers
    Collection<ContextAwareConfigurationPropertyMapper<Object>> mappers = propertyMapperResolver
        .resolveAll(contextResource).collect(Collectors.toList());

    // singleton caconfig
    if (metadata.isSingleton()) {
      ConfigurationData configData = configManager.getConfiguration(
          contextResource, metadata.getName());
      if (configData != null) {
        ConfigSingletonItem item = toSingletonItem(configData, request, mappers);
        if (!item.isEmpty()) {
          return item.toJsonObject();
        }
      }
    }

    // collection caconfig
    else {
      ConfigurationCollectionData configCollectionData = configManager.getConfigurationCollection(
          contextResource, metadata.getName());
      if (!configCollectionData.getItems().isEmpty()) {
        ConfigCollectionItem item = toCollectionItem(configCollectionData.getItems(), request, mappers);
        if (!item.isEmpty()) {
          return item.toJsonObject();
        }
      }
    }

    return null;
  }

  /**
   * Generate collection item for all configuration values.
   */
  private @NotNull ConfigCollectionItem toCollectionItem(@NotNull Collection<ConfigurationData> configurationDatas,
      @NotNull SlingHttpServletRequest request,
      @NotNull Collection<ContextAwareConfigurationPropertyMapper<Object>> mappers) {
    ConfigCollectionItem collectionItem = new ConfigCollectionItem();
    for (ConfigurationData configData : configurationDatas) {
      collectionItem.addItem(toSingletonItem(configData, request, mappers));
    }
    return collectionItem;
  }

  /**
   * Generate singleton item for all configuration values.
   */
  private @NotNull ConfigSingletonItem toSingletonItem(@NotNull ConfigurationData configData,
      @NotNull SlingHttpServletRequest request,
      @NotNull Collection<ContextAwareConfigurationPropertyMapper<Object>> mappers) {
    ConfigSingletonItem item = new ConfigSingletonItem();

    getExportedProperties(configData).forEach(property -> {
      if (property.isRequired()) {
        // mark required property
        item.addRequiredPropertyName(property.getName());
      }
      Object value = property.getValue();
      PropertyMetadata<?> metadata = property.getMetadata();
      if (value != null && metadata != null) {
        if (property.isNestedConfiguration()) {
          // special handling for nested configurations
          value = mapNestedConfiguration(value, metadata, request, mappers);
        }
        else {
          // map property value to target structure
          ContextAwareConfigurationPropertyMapper<Object> mapper = getMatchingMapper(value, metadata, request, mappers);
          if (mapper != null) {
            value = mapValue(value, metadata, request, mapper);
          }
        }
      }
      if (value != null) {
        item.put(property.getName(), value);
      }
    });

    return item;
  }

  /**
   * Calls property mapper. In case of object array, the mapper is called for each individual value.
   */
  private @Nullable Object mapValue(@NotNull Object value, @NotNull PropertyMetadata<?> metadata,
      @NotNull SlingHttpServletRequest request,
      @NotNull ContextAwareConfigurationPropertyMapper<Object> mapper) {
    if (value.getClass().isArray()) {
      List<Object> result = new ArrayList<>();
      int arrayLength = Array.getLength(value);
      for (int i = 0; i < arrayLength; i++) {
        Object valueItem = Array.get(value, i);
        Object mappedItem = mapper.map(valueItem, metadata, request);
        if (mappedItem != null) {
          result.add(mappedItem);
        }
      }
      if (result.isEmpty()) {
        return null;
      }
      else {
        return result;
      }
    }
    else {
      return mapper.map(value, metadata, request);
    }
  }

  /**
   * Get all properties to be exported.
   * Ignore system and hidden properties.
   */
  @SuppressWarnings("null")
  private @NotNull Stream<PropertyInfo> getExportedProperties(@NotNull ConfigurationData configData) {
    return configData.getPropertyNames().stream()
        .filter(propertyName -> !IGNORED_SYSTEM_PROPERTY_NAMES.matcher(propertyName).matches())
        .map(configData::getValueInfo)
        .filter(Objects::nonNull)
        .map(PropertyInfo::new)
        .filter(not(PropertyInfo::isHidden));
  }

  /**
   * Get property mapper that matches for this property.
   */
  private @Nullable ContextAwareConfigurationPropertyMapper<Object> getMatchingMapper(@NotNull Object value,
      @NotNull PropertyMetadata<?> metadata,
      @NotNull SlingHttpServletRequest request,
      @NotNull Collection<ContextAwareConfigurationPropertyMapper<Object>> mappers) {
    return mappers.stream()
        .filter(mapper -> mapper.accept(value, metadata, request))
        .findFirst().orElse(null);
  }

  /**
   * Special handling for nested configs or nested config collections
   */
  private @Nullable Object mapNestedConfiguration(@NotNull Object value,
      @NotNull PropertyMetadata<?> metadata,
      @NotNull SlingHttpServletRequest request,
      @NotNull Collection<ContextAwareConfigurationPropertyMapper<Object>> mappers) {

    if (metadata.getType().isArray()) {
      ConfigurationData[] configDatas = (ConfigurationData[])value;
      if (configDatas.length == 0) {
        return null;
      }
      return toCollectionItem(Arrays.asList(configDatas), request, mappers);
    }
    else {
      ConfigurationData configData = (ConfigurationData)value;
      return toSingletonItem(configData, request, mappers);
    }
  }

}
