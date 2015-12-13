package tv.teads.wiremock.extension.json.extractor

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock._
import com.ning.http.client.Response
import dispatch.{Future, Http, url}
import tv.teads.wiremock.extension.ExtensionSpec

import scala.concurrent.ExecutionContext.Implicits.global

class JsonExtractorSpec extends ExtensionSpec {

  val requests: List[(String, String, String, String)] = List(
    ("not found", """{}""", s"$${$$.single}", "null"),
    ("without interpretation", """{"single":"value"}""", s"""$$.single""", s"$$.single"),
    ("simple case", """{"single":"value"}""", s"$${$$.single}", "value"),
    ("nested value", """{"nested":{"single":"value"}}""", s"$${$$.nested.single}", "value"),
    ("array", """{"array":["1","2"]}""", s"$${$$.array[0]}", "1"),
    ("not found index", """{"array":["1","2"]}""", s"$${$$.array[2]}", "null"),
    ("multi replacements", """{"single":"value","array":["1","2"]}""", s"$${$$.single} $${$$.array[1]}", "value 2"),
    ("found and fallback", """{"single":"value"}""", s"$${$$.single|1}", "value"),
    ("not found and fallback", """{}""", s"$${$$.single|1}", "1"),
    ("array and fallback", """{"array":["1","2"]}""", s"$${$$.array[2]|3}", "3"),
    ("same replacements", """{"single":"value"}""", s"$${$$.single} $${$$.single}", "value value"),
    ("mixed found/not found", """{"single":"value"}""", s"$${$$.undefined} $${$$.single}", "null value"),
    ("nested replacements", s"""{"single":"value", "path":"$$.single"}""", s"$${$${$$.path}}", "value"),
    ("path as fallback", """{"single":"value"}""", s"$${$$.undefined|$$.single}", "value"),
    ("multi fallbacks", "{}", s"$${$$.undefined|$$.undefined|value}", "value"),
    ("complex template", "{}", s"""{"one":"value", "another":"$${$$.undefined|0}", "last": "one"}""", """{"one":"value", "another":"0", "last": "one"}"""),
    ("array replacement", "[1,2,3]", s"""$${$$}""", """[1, 2, 3]""")
  )

  "JsonExtractor" should "replace JSONPath in response body" in {
    requests.foreach {
      case (clue, requestBody, responseBody, result) ⇒
        val requestUrl = "/" + UUID.randomUUID().toString

        wireMockServer.givenThat(
          post(urlEqualTo(requestUrl))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody(responseBody)
                .withTransformers("json-extractor")
            )
        )

        val request: Future[Response] =
          Http(url(wireMockUrl + requestUrl)
            .<<(requestBody)
            .setContentType("application/json", "UTF-8"))

        whenReady(request) { request ⇒
          withClue("case [" + clue + "]") {
            request.getResponseBody shouldEqual result
          }
        }
    }
  }

}
