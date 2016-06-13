package tv.teads.wiremock.extension

import com.ning.http.client.Response
import dispatch.{Future, Http, Req, url ⇒ httpUrl}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

import scala.concurrent.ExecutionContext.Implicits.global

trait ExtensionSpec extends FlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  override implicit val patienceConfig = PatienceConfig(Span(5, Seconds), Span(100, Millis))

  override def beforeAll(): Unit = {
    if (!wireMockServer.isRunning) {
      wireMockServer.start()
    }
  }

  def stub[A](
    method:       String,
    url:          String,
    body:         String,
    transformers: String*
  )(
    f: ⇒ A
  ): A = {
    val newMappingBody: String =
      s"""
         |{
         |  "request": {
         |    "method": "$method",
         |    "url": "$url"
         |  },
         |  "response": {
         |    "status": 200,
         |    "body": "${body.replaceAllLiterally("\"", "\\\"")}",
         |    "transformers": [${transformers.mkString("\"", "\",\"", "\"")}]
         |  }
         |}
    """.stripMargin

    val newMappingRequest: Req =
      httpUrl(wireMockUrl + "/__admin/mappings/new")
        .POST
        .setHeader("Content-Type", "application/json; charset=utf-8")
        .setBody(newMappingBody)

    val http: FutureConcept[Response] = Http(newMappingRequest).map {
      case response if response.getStatusCode != 201 ⇒ throw new Exception(response.getResponseBody())
      case response                                  ⇒ response
    }

    whenReady(http)(_ ⇒ f)
  }

  def validate(request: Future[Response], result: String, clue: Any*) = {
    whenReady(request) { response ⇒
      withClue(clue.mkString("`", "` | `", "`")) {
        response.getResponseBody shouldEqual result
      }
    }
  }

}
