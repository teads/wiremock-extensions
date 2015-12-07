package tv.teads.wiremock.extension

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock._
import com.ning.http.client.Response
import dispatch.{Future, Http, url}

import scala.concurrent.ExecutionContext.Implicits.global

class CalculatorSpec extends ExtensionSpec {

  val requests: List[(String, String)] = List(
    (s"$${1+2}", "3"),
    (s"$${1-2}", "-1"),
    (s"$${1*2}", "2"),
    (s"$${1/2}", "0.5"),
    (s"$${1 + 2}", "3"),
    (s"$${1.1 + 2.2}", "3.3"),
    (s"$${1/0}", s"$${1/0}")
  )

  "Calculator" should "replace simple calculus in response body" in {
    requests.foreach {
      case (responseBody, result) ⇒
        val requestUrl = "/" + UUID.randomUUID().toString

        wireMockServer.givenThat(
          get(urlEqualTo(requestUrl))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody(responseBody)
                .withTransformers("calculator")
            )
        )

        val request: Future[Response] =
          Http(url(wireMockUrl + requestUrl))

        whenReady(request) { request ⇒
          withClue((responseBody, result)) {
            request.getResponseBody shouldEqual result
          }
        }
    }
  }

}
