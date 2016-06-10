package tv.teads.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._

package object extension {

  lazy val wireMockServer: WireMockServer = new WireMockServer(
    wireMockConfig()
      .port(12345)
      .extensions(new JsonExtractor, new Calculator)
  )

  lazy val wireMockUrl: String = "http://localhost:" + wireMockServer.port()

}