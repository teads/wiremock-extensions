package tv.teads.wiremock.extension

import java.util.UUID

import com.ning.http.client.Response
import dispatch.{Future, Http, url}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.matching.Regex

class RandomizerSpec extends ExtensionSpec {

  val requests: List[(String, Regex)] = List(
    ("@{RandomInteger}", """^\d*$""".r),
    ("@{RandomLong}", """^\d*$""".r),
    ("@{RandomString}", """^\w{10}$""".r),
    ("@{RandomBoolean}", """^(true|false)$""".r),
    ("@{RandomDouble}", """^(0(\.\d+)?|1(\.0+)?)$""".r),
    ("@{RandomFloat}", """^(0(\.\d+)?|1(\.0+)?)$""".r),
    ("@{RandomNotFound}", """\@\{RandomNotFound\}""".r)
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
