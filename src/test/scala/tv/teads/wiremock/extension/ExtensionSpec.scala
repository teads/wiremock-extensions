package tv.teads.wiremock.extension

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Span}
import org.scalatest.{BeforeAndAfterAll, Matchers, FlatSpec}

import scala.util.Random

trait ExtensionSpec extends FlatSpec with Matchers with BeforeAndAfterAll with ScalaFutures {

  def extensions: List[ResponseTransformer]

  override implicit val patienceConfig = PatienceConfig(Span(1000, Millis), Span(100, Millis))

  lazy val wireMockServer: WireMockServer = new WireMockServer(
    wireMockConfig()
      .port(Random.nextInt(1000) + 12345)
      .extensions(extensions: _*)
  )

  override def beforeAll(): Unit = {
    wireMockServer.start()
  }

  override def afterAll(): Unit = {
    wireMockServer.stop()
  }

}
