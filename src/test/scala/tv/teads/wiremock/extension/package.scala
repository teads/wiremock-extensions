package tv.teads.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._

package object extension {

  lazy val wireMockServer: WireMockServer = new WireMockServer(
    wireMockConfig()
      .port(12345)
      .extensions(new JsonExtractor, new Calculator, new FreeMarkerRenderer, new Randomizer)
  )

  lazy val wireMockUrl: String = "http://localhost:" + wireMockServer.port()

}
