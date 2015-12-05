package tv.teads.wiremock.extension

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.ning.http.client.Response
import dispatch.{Future, Http, url}

import scala.concurrent.ExecutionContext.Implicits.global

class CalculatorSpec extends ExtensionSpec {

  override val extensions: List[ResponseTransformer] = List(new Calculator)

  val requests: List[(String, String)] = List(
    ("1+2", "3"),
    ("1-2", "-1"),
    ("1*2", "2"),
    ("1/2", "0.5"),
    ("1 + 2", "3"),
    ("1.1 + 2.2", "3.3")
  )

  "Calculator" should "replace simple calculus in response body" in {
    requests.foreach {
      case (responseBody, result) ⇒
        val requestUrl = UUID.randomUUID().toString

        wireMockServer.givenThat(
          get(urlEqualTo("/" + requestUrl))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody(responseBody)
                .withTransformers("calculator")
            )
        )

        val request: Future[Response] =
          Http(url(s"http://localhost:${wireMockServer.port()}/$requestUrl"))

        whenReady(request) { request ⇒
          withClue((responseBody, result)) {
            request.getResponseBody shouldEqual result
          }
        }
    }
  }

}
