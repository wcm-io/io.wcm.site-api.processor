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

import static io.wcm.siteapi.processor.ProcessorConstants.PROCESSOR_CONFIG;
import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_SUFFIX;
import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_SUFFIX_PATTERN;
import static io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationProperties.PROPERTY_CONFIG_EMBEDDED;

import java.util.Collections;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.caconfig.management.ConfigurationManager;
import org.apache.sling.caconfig.spi.metadata.ConfigurationMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.FieldOption;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.wcm.siteapi.processor.JsonObjectProcessor;
import io.wcm.siteapi.processor.Processor;
import io.wcm.siteapi.processor.ProcessorRequestContext;
import io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationExport;
import io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationMapper;
import io.wcm.siteapi.processor.caconfig.impl.ConfigurationMetadataUtil;
import io.wcm.siteapi.processor.url.UrlBuilder;
import io.wcm.sling.commons.caservice.ContextAwareServiceCollectionResolver;
import io.wcm.sling.commons.caservice.ContextAwareServiceResolver;

/**
 * Generate context-aware configuration.
 */
@Designate(ocd = ContextAwareConfigurationProcessor.Config.class)
@Component(service = Processor.class, configurationPolicy = ConfigurationPolicy.REQUIRE, property = {
    PROPERTY_SUFFIX + "=" + PROCESSOR_CONFIG,
    PROPERTY_SUFFIX_PATTERN + "=^" + PROCESSOR_CONFIG + "(/.*)?$"
})
@ServiceRanking(-500)
public class ContextAwareConfigurationProcessor implements JsonObjectProcessor<Object> {

  @ObjectClassDefinition(
      name = "wcm.io Site API Context-Aware Configuration Processor",
      description = "Generates context-aware configuration.")
  @interface Config {

    @AttributeDefinition(
        name = "Enabled",
        description = "Processor is enabled.")
    boolean enabled() default false;

    @AttributeDefinition(
        name = "Shorten Config Names",
        description = "Whether to shorten the context aware configuration names to the part after the last '.'.")
    boolean shortenConfigNames() default true;

  }

  @Reference(cardinality = ReferenceCardinality.MULTIPLE, fieldOption = FieldOption.UPDATE,
      policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
  private SortedSet<ServiceReference<ContextAwareConfigurationExport>> caconfigExports = new ConcurrentSkipListSet<>(Collections.reverseOrder());

  @Reference
  private ConfigurationManager configManager;
  @Reference
  private ContextAwareConfigurationMapper contextAwareConfigurationMapper;
  @Reference
  private UrlBuilder urlBuilder;
  @Reference
  private ContextAwareServiceResolver serviceResolver;
  private ContextAwareServiceCollectionResolver<ContextAwareConfigurationExport, Void> caconfigExportCollectionResolver;

  static final String NOT_EMBEDDED_LINK_SUFFIX = ":link";

  private boolean shortenConfigNames;

  @Activate
  private void activate(Config config) {
    this.shortenConfigNames = config.shortenConfigNames();
    this.caconfigExportCollectionResolver = serviceResolver.getCollectionResolver(this.caconfigExports);
  }

  @Deactivate
  private void deactivate() {
    this.caconfigExportCollectionResolver.close();
  }

  @Override
  public @Nullable Object process(@NotNull ProcessorRequestContext context) {
    if (context.getSuffixExtension() != null) {
      return generateSingleConfig(context);
    }
    else {
      return generateAllConfigs(context);
    }
  }

  /**
   * Get map with all context-aware configurations configured for export.
   * Map keys are the (exported) configuration names.
   * @param context Context
   * @return Configuration map
   */
  private @NotNull SortedMap<String, Object> generateAllConfigs(@NotNull ProcessorRequestContext context) {
    SortedMap<String, Object> result = new TreeMap<>();
    getConfiguredConfigNames(context).forEach(configName -> {
      Object configObject = contextAwareConfigurationMapper.get(configName, context.getRequest());
      if (configObject != null) {
        String exportConfigName = getExportConfigName(configName);
        if (isEmbeddable(configName)) {
          result.put(exportConfigName, configObject);
        }
        else {
          String url = urlBuilder.build(context.getPage(), PROCESSOR_CONFIG, exportConfigName, context.getRequest());
          result.put(exportConfigName + NOT_EMBEDDED_LINK_SUFFIX, Map.of("url", url));
        }
      }
    });
    return result;
  }

  /**
   * Get a single named context-aware configuration.
   * @param context Context with suffix extension set pointing to the (exported) config name
   * @return Single configuration (or configuration collection) or null if no match found
   */
  private @Nullable Object generateSingleConfig(@NotNull ProcessorRequestContext context) {
    String suffixExtension = context.getSuffixExtension();
    return getConfiguredConfigNames(context)
        .filter(configName -> StringUtils.equals(getExportConfigName(configName), suffixExtension))
        .map(configName -> contextAwareConfigurationMapper.get(configName, context.getRequest()))
        .findFirst().orElse(null);
  }

  /**
   * @return Sorted stream with all caconfig names configured in any configuration.
   */
  private Stream<String> getConfiguredConfigNames(@NotNull ProcessorRequestContext context) {
    return caconfigExportCollectionResolver.resolveAll(context.getRequest())
        .flatMap(item -> item.getNames().stream())
        .sorted();
  }

  /**
   * @param configName Config name
   * @return Shortened or unshortened config name (depending on configuration)
   */
  private @NotNull String getExportConfigName(@NotNull String configName) {
    if (this.shortenConfigNames) {
      return toConfigNameWithoutPrefix(configName);
    }
    else {
      return configName;
    }
  }

  /**
   * Checks if the given configuration is embeddable in the main response, or if a link to it should be generated.
   * Embeddable is the default if {@link #PROPERTY_CONFIG_EMBEDDED} is not set.
   * @param configName Configuration name
   * @return true if embeddable
   */
  private boolean isEmbeddable(@NotNull String configName) {
    ConfigurationMetadata metadata = configManager.getConfigurationMetadata(configName);
    return ConfigurationMetadataUtil.isBoolean(metadata, PROPERTY_CONFIG_EMBEDDED, true);
  }

  /**
   * Returns the part after the last "." of the config name.
   * Usually this is the "simple class name" of a config name derived from the configuration class.
   * @param configName Full config name
   * @return Shortened config name
   */
  static @NotNull String toConfigNameWithoutPrefix(@NotNull String configName) {
    if (StringUtils.contains(configName, ".")) {
      return StringUtils.substringAfterLast(configName, ".");
    }
    else {
      return configName;
    }
  }

}