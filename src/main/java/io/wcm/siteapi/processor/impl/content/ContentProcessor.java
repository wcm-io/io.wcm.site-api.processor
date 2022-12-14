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
package io.wcm.siteapi.processor.impl.content;

import static com.adobe.cq.export.json.ExporterConstants.SLING_MODEL_EXTENSION;
import static com.adobe.cq.export.json.ExporterConstants.SLING_MODEL_SELECTOR;
import static io.wcm.siteapi.processor.ProcessorConstants.PROCESSOR_CONTENT;
import static io.wcm.siteapi.processor.ProcessorConstants.PROPERTY_SUFFIX;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;

import org.apache.sling.api.SlingHttpServletResponse;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.propertytypes.ServiceRanking;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

import io.wcm.siteapi.processor.Processor;
import io.wcm.siteapi.processor.ProcessorRequestContext;
import io.wcm.siteapi.processor.SlingHttpServletProcessor;

/**
 * Page content.
 * Forwards request to "model.json" version view of the page.
 */
@Designate(ocd = ContentProcessor.Config.class)
@Component(service = Processor.class, configurationPolicy = ConfigurationPolicy.REQUIRE,
    property = PROPERTY_SUFFIX + "=" + PROCESSOR_CONTENT)
@ServiceRanking(-500)
public class ContentProcessor implements SlingHttpServletProcessor {

  @ObjectClassDefinition(
      name = "wcm.io Site API Content Processor",
      description = "Provides model.json content of a page.")
  @interface Config {

    @AttributeDefinition(
        name = "Enabled",
        description = "Processor is enabled.")
    boolean enabled() default false;

  }

  @Override
  public void process(@NotNull ProcessorRequestContext context, @NotNull SlingHttpServletResponse response)
      throws ServletException, IOException {
    String modelJsonUri = context.getPage().getPath() + "." + SLING_MODEL_SELECTOR + "." + SLING_MODEL_EXTENSION;
    RequestDispatcher requestDispatcher = context.getRequest().getRequestDispatcher(modelJsonUri);
    if (requestDispatcher != null) {
      requestDispatcher.forward(context.getRequest(), response);
    }
  }

}
