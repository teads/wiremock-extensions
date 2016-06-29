# WireMock Extensions [![Build Status](https://travis-ci.org/teads/wiremock-extensions.svg?branch=master)](https://travis-ci.org/teads/wiremock-extensions)

wiremock-extensions is a set of extensions for WireMock.

## Installation

For now, wiremock-extensions is not published on Maven Central Repository.  
The only way is through the WireMock standalone process.

1. [Download WireMock Jar](https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/2.0.10-beta/wiremock-standalone-2.0.10-beta.jar)
2. [Download Extension Jar](https://github.com/teads/wiremock-extensions/releases/download/v0.7/wiremock-extensions_2.11-0.7.jar)
3. Run  
```
  java -cp "wiremock-standalone-2.0.10-beta.jar:wiremock-extensions_2.11-0.7.jar" \
       com.github.tomakehurst.wiremock.standalone.WireMockServerRunner \
       --extensions tv.teads.wiremock.extension.JsonExtractor,tv.teads.wiremock.extension.Calculator,tv.teads.wiremock.extension.FreeMarkerRenderer
```

## WireMock JSON Extractor

wiremock-json-extractor is a WireMock extension that can generate a response from a JSON request.  
It recognizes all JSONPaths from the response's template and try to replace them by the correct value
from the request. You can also specify a fallback value that will be use if nothing was found when searching
for the JSONPath.

```
{
  "request": {
    "method": "POST",
    "url": "/some/url"
  },
  "response": {
    "status": 200,
    "body": "I found ${$.value} for $.value. Sadly, I found nothing for ${$.undefined},
       so I will have to use the fallback value: ${$.undefined§3}",
    "transformers": ["json-extractor"]
  }
}
```

```
POST /some/url HTTP/1.1
Content-Type: application/json
{ "value": 12 }
```

```
HTTP/1.1 200 OK
I found 12 for $.value. Sadly, I found nothing for ${$.undefined},
 so I will have to use the fallback value: 3
```

You can check every supported operators on the [Gatling JSONPath Syntax](https://github.com/gatling/jsonpath#syntax) documentation.  

## WireMock Calculator

```
{
  "request": {
    "method": "GET",
    "url": "/some/url"
  },
  "response": {
    "status": 200,
    "body": "What is the value of 1+2*3? Simple, it is: ${1+2*3}",
    "transformers": ["calculator"]
  }
}
```

```
GET /some/url HTTP/1.1
```

```
HTTP/1.1 200 OK
What is the value of 1+2*3? Simple, it is: 7
```


## WireMock FreeMarkerRenderer

wiremock-freemarker-renderer is a WireMock extension that can generate a response from a FreeMarker template.  
It maps an incoming JSON request to a FreeMarker data model. $ can be used to call the root object. It allows
a syntax close to JSONPath.

```
{
  "request": {
    "method": "POST",
    "url": "/some/url"
  },
  "response": {
    "status": 200,
    "body": "I found ${$.value} for $.value. Sadly, I found nothing for ${$.undefined!"$.undefined"}, 
       so I will have to use the fallback value: ${$.undefined!3}",
    "transformers": ["freemarker-renderer"]
  }
}
```

```
POST /some/url HTTP/1.1
Content-Type: application/json
{ "value": 12 }
```

```
HTTP/1.1 200 OK
I found 12 for $.value. Sadly, I found nothing for $.undefined, 
 so I will have to use the fallback value: 3
```

You can check all the possibilities on the [FreeMarker](http://freemarker.org/docs/dgui.html) documentation.