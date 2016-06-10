package tv.teads.wiremock.extension

import com.ning.http.client.Response
import dispatch.Future
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

trait ExtensionSpec extends FlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  override implicit val patienceConfig = PatienceConfig(Span(1000, Millis), Span(100, Millis))

  override def beforeAll(): Unit = {
    if (!wireMockServer.isRunning) {
      wireMockServer.start()
    }
  }

  def validate(request: Future[Response], result: String, clue: Any*) = {
    whenReady(request) { response ⇒
      withClue(clue.mkString("`", "` | `", "`")) {
        response.getResponseBody shouldEqual result
      }
    }
  }

}
