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
package io.wcm.siteapi.processor.caconfig.impl.property;

import static io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationProperties.PROPERTY_JSON_RAW_VALUE;

import java.io.IOException;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.caconfig.spi.metadata.PropertyMetadata;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationProperties;
import io.wcm.siteapi.processor.caconfig.ContextAwareConfigurationPropertyMapper;
import io.wcm.siteapi.processor.caconfig.impl.ConfigurationMetadataUtil;
import io.wcm.siteapi.processor.util.JsonObjectMapper;

/**
 * Checks if the given property value is a string value and the property is marked with
 * {@link ContextAwareConfigurationProperties#PROPERTY_JSON_RAW_VALUE}. If that is the case
 * the string value is expected to be a JSON String and converted to data structure that can be
 * serialized as JSON. If the value is invalid, no value is returned.
 */
@Component(service = ContextAwareConfigurationPropertyMapper.class)
@ServiceRanking(-500)
public class JsonRawValuePropertyMapper implements ContextAwareConfigurationPropertyMapper<Map<String, Object>> {

  private static final Logger log = LoggerFactory.getLogger(JsonRawValuePropertyMapper.class);

  @Reference
  private JsonObjectMapper jsonObjectMapper;

  @Override
  public boolean accept(@NotNull Object value,
      @NotNull PropertyMetadata<?> metadata, @NotNull SlingHttpServletRequest request) {
    return ConfigurationMetadataUtil.isBoolean(metadata, PROPERTY_JSON_RAW_VALUE, false);
  }

  @Override
  public @Nullable Map<String, Object> map(@NotNull Object value,
      @NotNull PropertyMetadata<?> metadata, @NotNull SlingHttpServletRequest request) {
    return parseJson(value);
  }

  @SuppressWarnings("CQRules:CQBP-44---WrongLogLevelInCatchBlock")
  private @Nullable Map<String, Object> parseJson(@NotNull Object value) {
    if (value instanceof String) {
      String stringValue = (String)value;
      if (StringUtils.isNotBlank(stringValue)) {
        try {
          return jsonObjectMapper.parseToMap(stringValue);
        }
        catch (IOException ex) {
          log.debug("Failed to parse JSON value: {}", value, ex);
        }
      }
    }
    return null;
  }

}
