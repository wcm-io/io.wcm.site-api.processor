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
package io.wcm.siteapi.processor.url;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Validates and parses suffix with '.json' extension.
 * <p>
 * The suffix string is split in two parts:
 * </p>
 * <ul>
 * <li>The main suffix part which is matched with the processor (e.g. 'content', 'caconfig')</li>
 * <li>Optional: A suffix extension separated by / (e.g. context-aware configuration name)</li>
 * </ul>
 * <p>
 * Suffix string Examples:
 * </p>
 * <ul>
 * <li><code>/content.json</code> -&gt; suffix=content</li>
 * <li><code>/caconfig/MyConfig.json</code> -&gt; suffix=content, suffix extension=MyConfig</li>
 * </ul>
 */
@ProviderType
public final class JsonSuffix {

  private static final Pattern SUFFIX_AND_EXTENSION = Pattern.compile("^/([^/]+)(/(.*))?\\.json$");
  private static final int GROUP_SUFFIX = 1;
  private static final int GROUP_SUFFIX_EXTENSION = 3;

  private final String suffix;
  private final String suffixExtension;

  private JsonSuffix(@NotNull String suffix, @Nullable String suffixExtension) {
    this.suffix = suffix;
    this.suffixExtension = suffixExtension;
  }

  /**
   * @return Suffix
   */
  public @NotNull String getSuffix() {
    return this.suffix;
  }

  /**
   * @return Suffix extension
   */
  public @Nullable String getSuffixExtension() {
    return this.suffixExtension;
  }

  /**
   * Ensure suffix ends with ".json". Returns suffix without slash and extension.
   * @param suffixString Suffix string
   * @return Suffix or null if suffix string is invalid
   */
  public static @Nullable JsonSuffix parse(@Nullable String suffixString) {
    if (suffixString != null) {
      Matcher matcher = SUFFIX_AND_EXTENSION.matcher(suffixString);
      if (matcher.matches()) {
        String suffix = matcher.group(GROUP_SUFFIX);
        String suffixExtension = matcher.group(GROUP_SUFFIX_EXTENSION);
        return new JsonSuffix(suffix, suffixExtension);
      }
    }
    return null;
  }

  /**
   * Build suffix with JSON extension.
   * @param suffix Suffix
   * @return Suffix with slash and extension.
   */
  public static @NotNull String build(@NotNull String suffix) {
    return build(suffix, null);
  }

  /**
   * Build suffix with JSON extension.
   * @param suffix Suffix
   * @param suffixExtension Suffix extension
   * @return Suffix with slash and extension.
   */
  public static @NotNull String build(@NotNull String suffix, @Nullable String suffixExtension) {
    StringBuilder sb = new StringBuilder();
    sb.append('/').append(suffix);
    if (StringUtils.isNotEmpty(suffixExtension)) {
      sb.append('/').append(suffixExtension);
    }
    sb.append(".json");
    return sb.toString();
  }

}
