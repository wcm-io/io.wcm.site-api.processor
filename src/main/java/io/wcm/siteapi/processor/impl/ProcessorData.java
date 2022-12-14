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

import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_ENABLED;
import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_SUFFIX;
import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_SUFFIX_PATTERN;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.siteapi.processor.Processor;
import io.wcm.siteapi.processor.ProcessorMetadata;
import io.wcm.siteapi.processor.util.impl.DictionaryUtil;

/**
 * Wraps a processor instance with compiled and validated suffix pattern.
 */
class ProcessorData implements ProcessorMetadata {

  private static final Logger log = LoggerFactory.getLogger(ProcessorData.class);

  private final Processor processor;
  private final String suffix;
  private final Pattern suffixPattern;
  private final boolean enabled;
  private final Map<String, Object> properties;

  ProcessorData(@NotNull ServiceReference<Processor> serviceReference, @Nullable Processor processor) {
    this.processor = processor;
    this.suffix = getSuffix(serviceReference, this.processor);
    this.suffixPattern = getSuffixPattern(serviceReference, this.suffix, this.processor);
    this.enabled = getEnabled(serviceReference);
    this.properties = DictionaryUtil.toMap(serviceReference.getProperties());
  }

  public @Nullable Processor getProcessor() {
    return processor;
  }

  public boolean isValid() {
    return processor != null && suffix != null && suffixPattern != null && enabled;
  }

  @Override
  public @NotNull String getSuffix() {
    return this.suffix;
  }

  @Override
  public @NotNull Class<? extends Processor> getProcessorClass() {
    return processor.getClass();
  }

  @Override
  public @NotNull Map<String, Object> getProperties() {
    return properties;
  }

  public boolean matches(@NotNull String givenSuffix) {
    return suffixPattern.matcher(givenSuffix).matches();
  }

  private static @Nullable String getSuffix(ServiceReference<Processor> serviceReference,
      Processor processor) {
    String suffix = (String)serviceReference.getProperty(PROPERTY_SUFFIX);
    if (StringUtils.isBlank(suffix)) {
      log.warn("Ignoring processor {} without {} property", processor, PROPERTY_SUFFIX);
      suffix = null;
    }
    return suffix;
  }

  private static @Nullable Pattern getSuffixPattern(ServiceReference<Processor> serviceReference, String suffix,
      Processor processor) {
    if (suffix == null) {
      return null;
    }
    String suffixPattern = (String)serviceReference.getProperty(PROPERTY_SUFFIX_PATTERN);
    if (StringUtils.isBlank(suffixPattern)) {
      suffixPattern = "^" + Pattern.quote(suffix) + "$";
    }
    try {
      return Pattern.compile(suffixPattern);
    }
    catch (PatternSyntaxException ex) {
      log.warn("Ignoring processor {} with invalid suffix pattern: {} - {}", processor, suffixPattern, ex.getMessage());
    }
    return null;
  }

  private static boolean getEnabled(ServiceReference<Processor> serviceReference) {
    Object enabled = serviceReference.getProperty(PROPERTY_ENABLED);
    if (enabled == null) {
      return true;
    }
    else if (enabled instanceof Boolean) {
      return (Boolean)enabled;
    }
    else {
      return BooleanUtils.toBoolean(enabled.toString());
    }
  }

  @Override
  public String toString() {
    return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
        .append("processor", processor)
        .append("suffix", suffix)
        .append("suffixPattern", suffixPattern)
        .toString();
  }

}
