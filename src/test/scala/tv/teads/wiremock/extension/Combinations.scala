package tv.teads.wiremock.extension

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.ning.http.client.Response
import dispatch.{Future, Http, url}

import scala.concurrent.ExecutionContext.Implicits.global

class Combinations extends ExtensionSpec {

  override def extensions: List[ResponseTransformer] = List(new JsonExtractor, new Calculator)

  val requests: List[(String, String, String)] = List(
    ("""{"single":"value"}""", "$.single", "value"),
    ("""{}""", "1+2", "3"),
    ("""{"single":1}""", "$.single + 2", "3")
  )

  "JsonExtractor and Calculator" should "combine" in {
    requests.foreach {
      case (requestBody, responseBody, result) ⇒
        val requestUrl = UUID.randomUUID().toString

        wireMockServer.givenThat(
          post(urlEqualTo("/" + requestUrl))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody(responseBody)
                .withTransformers("json-extractor", "calculator")
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
