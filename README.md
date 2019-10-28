# [Gradle Plugin Portal](https://plugins.gradle.org/) API

[![Build Status](https://travis-ci.com/gradle-plugins/portal-api.svg?branch=master)](https://travis-ci.com/gradle-plugins/portal-api)

This repository crawl the plugin portal to create a static REST API for everyone to use.
Feel free to help with the effort and contribute any change you feel the REST API should have.
Please note this REST API is a strictly vanilla API of https://plugins.gradle.org.

Future plan, will see an enhanced API with static analysis information of the plugins (e.g. security vulnerability, supported Gradle version, etc.) as well as user provided information (e.g. end of life of plugins, etc.).

## REST API

The API is static and hosted by GitHub Pages.
Consequently, the API has no dynamic content.
There is no parameters supported or headers.

The reaction time - the time it takes for a change on the Gradle Plugin Portal to appear in this API - is at most 24h.
We can work on this delay if and when it will become a problem.
At the moment, there is a scheduled Travis CI job that take cares of scrapping.


### List plugins

```
GET /plugins
```

#### Response

```
Status: 200 OK
----
{
  "dev.gradleplugins.java-gradle-plugin": "https://gradleplugins.org/portal-api/plugins/dev.gradleplugins.groovy-gradle-plugin/",
  "dev.gradleplugins.groovy-gradle-plugin": "https://gradleplugins.org/portal-api/plugins/dev.gradleplugins.groovy-gradle-plugin/",
  "dev.gradleplugins.kotlin-gradle-plugin": "https://gradleplugins.org/portal-api/plugins/dev.gradleplugins.groovy-gradle-plugin/",
  ...
}
```

### Get plugin

```
GET /plugins/:pluginId
```

#### Response

```
Status: 200 OK
----
{
  "pluginId": "dev.gradleplugins.java-gradle-plugin",
  "description": "Fast track development of Gradle plugins in Java",
  "latestVersion": "https://gradleplugins.org/portal-api/plugins/dev.gradleplugins.java-gradle-plugin/0.0.21/",
  "versions": {
    "0.0.21": "https://gradleplugins.org/portal-api/plugins/dev.gradleplugins.java-gradle-plugin/0.0.21/",
    "0.0.20": "https://gradleplugins.org/portal-api/plugins/dev.gradleplugins.java-gradle-plugin/0.0.20/",
    "0.0.19": "https://gradleplugins.org/portal-api/plugins/dev.gradleplugins.java-gradle-plugin/0.0.19/",
    ...
  }
}
```

### Get plugin version

```
GET /plugins/:pluginId/:version
```

#### Response

```
Status: 200 OK
----
{
  "pluginId": "dev.gradleplugins.java-gradle-plugin",
  "version": "0.0.21",
  "notation": "dev.gradleplugins:gradle-plugin-development:0.0.21"
}
```
