package tv.teads.wiremock.extension

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

trait ExtensionSpec extends FlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  lazy val wireMockServer: WireMockServer = ExtensionSpec.wireMockServer
  lazy val wireMockUrl: String = ExtensionSpec.wireMockUrl

  override implicit val patienceConfig = PatienceConfig(Span(1000, Millis), Span(100, Millis))

  override def beforeAll(): Unit = {
    if (!wireMockServer.isRunning) {
      wireMockServer.start()
    }
  }

}

object ExtensionSpec {

  lazy val wireMockServer: WireMockServer = new WireMockServer(
    wireMockConfig()
      .port(12345)
      .extensions(new JsonExtractor, new Calculator)
  )

  lazy val wireMockUrl: String = "http://localhost:" + wireMockServer.port()

}