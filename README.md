# WireMock JSONPath Extractor [![Build Status](https://jenkins.teads.net/buildStatus/icon?job=wiremock-json-extractor_master)](https://jenkins.teads.net/job/wiremock-json-extractor_master/)

wiremock-json-extractor is a WireMock extension that can generate a response from a JSON request.  
It recognize all JSONPaths from the response's template and try to replace them by the correct value
from the request.

## Installation

For now, wiremock-json-extractor is not published on Maven Central Repository.
The only way is through the WireMock standalone process.

1. [Download WireMock Jar](https://repo1.maven.org/maven2/com/github/tomakehurst/wiremock/1.58/wiremock-1.58-standalone.jar)
2. [Download Extension Jar](https://github.com/ebuzzing/wiremock-json-extractor/releases/download/v0.3/wiremock-json-extractor_2.11-0.3.jar)
3. Run  
```
  java -cp "wiremock-1.58-standalone.jar:wiremock-json-extractor_2.11-0.3.jar"
       com.github.tomakehurst.wiremock.standalone.WireMockServerRunner 
       --extensions tv.teads.wiremock.extension.JsonExtractor
```

## Usage

Given the following stub
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

The following request
```
POST /some/url HTTP/1.1
Content-Type: application/json
{ "value": 12 }
```

Will produce
```
HTTP/1.1 200 OK
I found 12 for $.value. But nothing for ${$.undefined}
```
