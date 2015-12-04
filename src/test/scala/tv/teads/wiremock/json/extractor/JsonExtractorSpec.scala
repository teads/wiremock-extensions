package tv.teads.wiremock.json.extractor

import java.util.UUID

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import com.ning.http.client.Response
import dispatch.{Future, Http, url}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

class JsonExtractorSpec extends FlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  override implicit val patienceConfig = PatienceConfig(Span(500, Millis), Span(50, Millis))

  val wireMockServer: WireMockServer = new WireMockServer(
    wireMockConfig()
      .extensions(new JsonExtractor)
  )

  override def beforeAll(): Unit = {
    wireMockServer.start()
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
  }

  val requests: List[(String, String, String)] = List(
    ("""{}""", "$.single", "$.single"), // not found case
    ("""{"single":"value"}""", """\$.single""", "$.single"), // with escape
    ("""{"single":"value"}""", "$.single", "value"), // simple case
    ("""{"nested":{"single":"value"}}""", "$.nested.single", "value"), // with nested value
    ("""{"array":["1","2"]}""", "$.array[0]", "1"), // with array
    ("""{"single":"value","array":["1","2"]}""", "$.single $.array[1]", "value 2"), // with multi replacements
    ("""{"single":"value"}""", "$.single $.single", "value value") // with multi same replacements
  )

  "JsonExtractor" should "replace JSONPath in response body" in {
    requests.foreach {
      case (requestBody, responseBody, result) =>
        val requestUrl = UUID.randomUUID().toString

        wireMockServer.givenThat(
          post(urlEqualTo("/" + requestUrl))
            .willReturn(
              aResponse()
                .withHeader("Content-Type", "text/plain")
                .withBody(responseBody)
                .withTransformers("json-extractor"))
        )

        val request: Future[Response] =
          Http(url("http://localhost:8080/" + requestUrl)
            .<<(requestBody)
            .setContentType("application/json", "UTF-8"))

        whenReady(request) { request =>
          withClue((requestBody, responseBody, result)) {
            request.getResponseBody shouldEqual result
          }
        }
    }
  }

}
