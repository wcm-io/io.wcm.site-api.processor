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
package io.wcm.siteapi.processor.util.impl;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

/**
 * Collection of test data for JSON serialization/parsing.
 */
public final class JsonTestData {

  public static final PojoExample POJO = new PojoExample("value1", true,
      List.of(new PojoChildExample("child1", true), new PojoChildExample("child2", false)));

  public static final String POJO_JSON = "{\"value\":\"value1\",\"flag\":true,"
      + "\"children\":[{\"value\":\"child1\",\"flag\":true},{\"value\":\"child2\",\"flag\":false}]}";

  public static final Map<String, Object> POJO_MAP = Map.of("value", "value1", "flag", true,
      "children", List.of(
          Map.of("value", "child1", "flag", true),
          Map.of("value", "child2", "flag", false)));

  private JsonTestData() {
    // constants only
  }

  public static class PojoExample {

    private final String value;
    private final boolean flag;
    private final List<@NotNull PojoChildExample> children;

    PojoExample(String value, boolean flag, List<@NotNull PojoChildExample> children) {
      this.value = value;
      this.flag = flag;
      this.children = children;
    }

    public String getValue() {
      return this.value;
    }

    public boolean isFlag() {
      return this.flag;
    }

    public List<@NotNull PojoChildExample> getChildren() {
      return this.children;
    }

  }

  public static class PojoChildExample {

    private final String value;
    private final boolean flag;

    PojoChildExample(String value, boolean flag) {
      this.value = value;
      this.flag = flag;
    }

    public String getValue() {
      return this.value;
    }

    public boolean isFlag() {
      return this.flag;
    }

  }

}
