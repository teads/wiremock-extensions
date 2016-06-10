package tv.teads.wiremock.extension

import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time._
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

trait ExtensionSpec extends FlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  override implicit val patienceConfig = PatienceConfig(Span(5, Seconds), Span(100, Millis))

  override def beforeAll(): Unit = {
    if (!wireMockServer.isRunning) {
      wireMockServer.start()
    }
  }

}
