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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import io.wcm.siteapi.processor.ProcessorRequestContext;
import io.wcm.siteapi.processor.textcontext.AppAemContext;
import io.wcm.testing.mock.aem.junit5.AemContext;
import io.wcm.testing.mock.aem.junit5.AemContextExtension;

@ExtendWith(AemContextExtension.class)
class ProcessorRequestContextImplTest {

  private AemContext context = AppAemContext.newAemContext();

  @BeforeEach
  void setUp() {
    context.currentPage(context.create().page("/content/page1"));
  }

  @Test
  @SuppressWarnings("null")
  void testProperties() {
    ProcessorRequestContext underTest = new ProcessorRequestContextImpl(
        "suffix1", "suffixExt1", context.request(), context.pageManager(), context.currentPage());

    assertEquals("suffix1", underTest.getSuffix());
    assertEquals("suffixExt1", underTest.getSuffixExtension());
    assertSame(context.request(), underTest.getRequest());
    assertSame(context.pageManager(), underTest.getPageManager());
    assertSame(context.currentResource(), underTest.getResource());
    assertSame(context.currentPage(), underTest.getPage());
  }

}
