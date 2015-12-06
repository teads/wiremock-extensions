# WireMock Extensions [![Build Status](https://jenkins.teads.net/buildStatus/icon?job=wiremock-json-extractor_master)](https://jenkins.teads.net/job/wiremock-json-extractor_master/)

wiremock-extensions is a set of extensions for WireMock.

## Installation

For now, wiremock-extensions is not published on Maven Central Repository.  
The only way is through the WireMock standalone process.

1. [Download WireMock Jar](https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock/1.58/wiremock-1.58-standalone.jar)
2. [Download Extension Jar](https://github.com/ebuzzing/wiremock-json-extractor/releases/download/v0.3/wiremock-extensions_2.11-0.3.jar)
3. Run  
```
  java -cp "wiremock-1.58-standalone.jar:wiremock-extensions_2.11-0.3.jar" \
       com.github.tomakehurst.wiremock.standalone.WireMockServerRunner \
       --extensions tv.teads.wiremock.extension.JsonExtractor,tv.teads.wiremock.extension.Calculator
```

## WireMock JSON Extractor

wiremock-json-extractor is a WireMock extension that can generate a response from a JSON request.  
It recognizes all JSONPaths from the response's template and try to replace them by the correct value
from the request.

```
{
  "request": {
    "method": "POST",
    "url": "/some/url"
  },
  "response": {
    "status": 200,
    "body": "I found ${$.value} for $.value. But nothing for ${$.undefined}",
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
I found 12 for $.value. But nothing for ${$.undefined}
```


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
