package tv.teads.wiremock.extension

import java.util.UUID

import com.ning.http.client.Response
import dispatch.{Future, Http, url}

import scala.concurrent.ExecutionContext.Implicits.global

class CombinationsSpec extends ExtensionSpec {

  val requests: List[(String, String, String)] = List(
    ("""{"single":"value"}""", s"$${$$.single}", "value"),
    ("""{}""", s"$${1+2}", "3"),
    ("""{"single":1}""", s"$${$${$$.single} + 2}", "3")
  )

  "JsonExtractor and Calculator" should "combine" in {
    requests.foreach {
      case (requestBody, responseBody, result) ⇒
        val requestUrl = "/" + UUID.randomUUID().toString

        StubHelper.stub(wireMockServer, requestUrl, responseBody, "json-extractor", "calculator")

        val request: Future[Response] =
          Http(url(wireMockUrl + requestUrl)
            .<<(requestBody)
            .setContentType("application/json", "UTF-8"))

        validate(
          request = request,
          result = result,
          clue = requestBody, responseBody, result
        )
    }
  }

}
