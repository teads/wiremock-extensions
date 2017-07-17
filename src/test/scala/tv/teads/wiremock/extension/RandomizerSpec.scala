package tv.teads.wiremock.extension

import java.util.UUID

import com.ning.http.client.Response
import dispatch.{Future, Http, url}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.matching.Regex

class RandomizerSpec extends ExtensionSpec {

  val requests: List[(String, Regex)] = List(
    (s"$${RandomInteger}", """^\d*$""".r),
    (s"$${RandomLong}", """^\d*$""".r),
    (s"$${RandomString}", """^\w{10}$""".r),
    (s"$${RandomBoolean}", """^(true|false)$""".r),
    (s"$${RandomDouble}", """^(0(\.\d+)?|1(\.0+)?)$""".r),
    (s"$${RandomFloat}", """^(0(\.\d+)?|1(\.0+)?)$""".r),
    (s"$${RandomNotFound}", """\$\{RandomNotFound\}""".r)
  )

  "Randomizer" should "replace random placeholders in response body" in {
    requests.foreach {
      case (responseBody, result) ⇒
        val requestUrl = "/" + UUID.randomUUID().toString

        stub("GET", requestUrl, responseBody, "randomizer") {
          val request: Future[Response] = Http(url(wireMockUrl + requestUrl))

          validate(
            request = request,
            result = result,
            clue = responseBody, result
          )
        }
    }
  }

}
