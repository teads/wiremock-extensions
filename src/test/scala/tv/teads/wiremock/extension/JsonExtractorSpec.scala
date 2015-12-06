package tv.teads.wiremock.extension

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.ning.http.client.Response
import dispatch.{Future, Http, url}

import scala.concurrent.ExecutionContext.Implicits.global

class JsonExtractorSpec extends ExtensionSpec {

  override val extensions: List[ResponseTransformer] = List(new JsonExtractor)

  val requests: List[(String, String, String)] = List(
    ("""{}""", s"$${$$.single}", s"$${$$.single}"), // not found case
    ("""{"single":"value"}""", s"""$$.single""", s"$$.single"), // without interpretation
    ("""{"single":"value"}""", s"$${$$.single}", "value"), // simple case
    ("""{"nested":{"single":"value"}}""", s"$${$$.nested.single}", "value"), // with nested value
    ("""{"array":["1","2"]}""", s"$${$$.array[0]}", "1"), // with array
    ("""{"single":"value","array":["1","2"]}""", s"$${$$.single} $${$$.array[1]}", "value 2"), // with multi replacements
    ("""{"single":"value"}""", s"$${$$.single} $${$$.single}", "value value") // with multi same replacements
  )

  "JsonExtractor" should "replace JSONPath in response body" in {
    requests.foreach {
      case (requestBody, responseBody, result) ⇒
        val requestUrl = UUID.randomUUID().toString

        wireMockServer.givenThat(
          post(urlEqualTo("/" + requestUrl))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody(responseBody)
                .withTransformers("json-extractor")
            )
        )

        val request: Future[Response] =
          Http(url(s"http://localhost:${wireMockServer.port()}/$requestUrl")
            .<<(requestBody)
            .setContentType("application/json", "UTF-8"))

        whenReady(request) { request ⇒
          withClue((requestBody, responseBody, result)) {
            request.getResponseBody shouldEqual result
          }
        }
    }
  }

}
