## Site API Processor usage

### Configuring built-in processors

You can enable the built-in processors by providing an OSGi configuration:

```
  io.wcm.siteapi.processor.impl.index.IndexProcessor
    enabled=B"true"

  io.wcm.siteapi.processor.impl.content.ContentProcessor
    enabled=B"true"

  io.wcm.siteapi.processor.impl.caconfig.ContextAwareConfigurationProcessor
    enabled=B"true"
```

For the `config` processor you can configure which Context-Aware configuration names should be exported:

```
  io.wcm.siteapi.processor.impl.caconfig.ContextAwareConfigurationExportImpl-XXX
    names=["x.y.z.MyConfig1", "x.y.z.MyConfig2"]
```

It's a factory configuration, so replace XXX with a project-specific suffix.

To ensure a consistent JSON property/element ordering it is recommended to reconfigure the Sling Models Jackson exporter:

```
  org.apache.sling.models.jacksonexporter.impl.JacksonExporter
    mapping.options=[
      "SerializationFeature.ORDER_MAP_ENTRIES_BY_KEYS\=true",
      "MapperFeature.SORT_PROPERTIES_ALPHABETICALLY\=true"]
```


### Implement Processor serving JSON objects

For most use cases you build your JSON response using Java objects or Map structures. This is supported by using the `JsonObjectProcessor':

```java
@Component(service = Processor.class, property = {
    ProcessorConstants.PROPERTY_SUFFIX + "=myprocessor",
    ProcessorConstants.PROPERTY_ENABLED + "=true"
})
public class MyProcessor implements JsonObjectProcessor<MyResponse> {
  @Override
  public @Nullable MyResponse process(@NotNull ProcessorRequestContext context) {
    // implement
  }
}
```

If you return `null`, the Site API returns HTTP 404. You can use this e.g. to further validate the request context, page hierarchy or other input parameters to check if the request is valid.

If you return an actual object, this is serialized to JSON and returned.

It's easy to write unit tests for this type of processor implementations as you can write test against the Java API of the response, and do not have to parse and validate the returned JSON structure.

Example implementations:

* [IndexProcessor](https://github.com/wcm-io/io.wcm.site-api.processor/blob/develop/src/main/java/io/wcm/siteapi/processor/impl/index/IndexProcessor.java)
* [ContextAwareConfigurationProcessor](https://github.com/wcm-io/io.wcm.site-api.processor/blob/develop/src/main/java/io/wcm/siteapi/processor/impl/caconfig/ContextAwareConfigurationProcessor.java)
* [NavigationProcessor](https://github.com/wcm-io/io.wcm.site-api.handler/blob/develop/src/main/java/io/wcm/siteapi/handler/processor/impl/navigation/NavigationProcessor.java)


### Implement Processor serving any response

If you need more control about the response, you can implement the `SlingHttpServletProcessor` interface:

```java
@Component(service = Processor.class, property = {
    ProcessorConstants.PROPERTY_SUFFIX + "=myprocessor",
    ProcessorConstants.PROPERTY_ENABLED + "=true"
})
public class MyProcessor implements SlingHttpServletProcessor {
@Override
  public void process(@NotNull ProcessorRequestContext context, @NotNull SlingHttpServletResponse response)
      throws ServletException, IOException {
    // implement
  }
}
```

In this case you have to take over full control over the servlet response and the data returned.

Example implementations:
* [ContentProcessor](https://github.com/wcm-io/io.wcm.site-api.processor/blob/develop/src/main/java/io/wcm/siteapi/processor/impl/content/ContentProcessor.java)


### Customize Context-Aware Configuration Mapping

By default, all Context-Aware configuration structures are transformed in a sensible way to JSON structures, including more complex use cases like configuration collections and nested configurations.

You can customize tha mapping for individual properties by implementing the interface `ContextAwareConfigurationPropertyMapper`.

Example implementations:

* [JsonRawValuePropertyMapper](https://github.com/wcm-io/io.wcm.site-api.processor/blob/develop/src/main/java/io/wcm/siteapi/processor/caconfig/impl/property/JsonRawValuePropertyMapper.java)
* [ContentPathPropertyMapper](https://github.com/wcm-io/io.wcm.site-api.handler/blob/develop/src/main/java/io/wcm/siteapi/handler/caconfig/impl/property/ContentPathPropertyMapper.java)
