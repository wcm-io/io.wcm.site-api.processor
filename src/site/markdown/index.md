## About Site API Processor

Processor API for Headless AEM projects based on AEM Sites.

[![Maven Central](https://img.shields.io/maven-central/v/io.wcm/io.wcm.site-api.processor)](https://repo1.maven.org/maven2/io/wcm/io.wcm.site-api.processor/)


### Documentation

* [Usage][usage]
* [API documentation][apidocs]
* [Changelog][changelog]


### Overview

The Site API Processor provides:

* Implement you own processors to serve JSON API requests within the Site API
* Built-in `index` processor that provides links to all registered processors
* Built-in `content` processor that provides a facade to `.model.json` view within Site API
* Built-in `config` Context-Aware Configuration processor
* Maps any Context-Aware Configuration structure to JSON (allowing to customize the response based on context-aware configuration definition properties)
* Based on [Site API General Concepts][siteapi-general-concepts]
* Based on [Context-Aware Services][wcmio-sling-context-aware-services] for provider type service interfaces

### AEM Version Support Matrix

|Site API Processor version |AEM version supported
|---------------------------|----------------------
|1.0.0 or higher            |AEM 6.5.7+, AEMaaCS


### Dependencies

To use this module you have to deploy also:

|---|---|---|
| [wcm.io Sling Commons](https://repo1.maven.org/maven2/io/wcm/io.wcm.sling.commons/) | [![Maven Central](https://img.shields.io/maven-central/v/io.wcm/io.wcm.sling.commons)](https://repo1.maven.org/maven2/io/wcm/io.wcm.sling.commons/) |


### GitHub Repository

Sources: https://github.com/wcm-io/io.wcm.site-api.processor


[usage]: usage.html
[apidocs]: apidocs/
[changelog]: changes-report.html
[siteapi-general-concepts]: https://wcm.io/site-api/general-concepts.html
[wcmio-sling-context-aware-services]: https://wcm.io/sling/commons/context-aware-services.html
