package tv.teads.wiremock.extension.combinations

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock._
import com.ning.http.client.Response
import dispatch.{Future, Http, url}
import tv.teads.wiremock.extension.ExtensionSpec

import scala.concurrent.ExecutionContext.Implicits.global

class Combinations extends ExtensionSpec {

  val requests: List[(String, String, String)] = List(
    ("""{"single":"value"}""", s"$${$$.single}", "value"),
    ("""{}""", s"$${1+2}", "3"),
    ("""{"single":1}""", s"$${$${$$.single} + 2}", "3")
  )

  "JsonExtractor and Calculator" should "combine" in {
    requests.foreach {
      case (requestBody, responseBody, result) ⇒
        val requestUrl = "/" + UUID.randomUUID().toString

        wireMockServer.givenThat(
          post(urlEqualTo(requestUrl))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody(responseBody)
                .withTransformers("json-extractor", "calculator")
            )
        )

        val request: Future[Response] =
          Http(url(wireMockUrl + requestUrl)
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
