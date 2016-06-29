package tv.teads.wiremock.extension

import java.util.UUID

import com.ning.http.client.Response
import dispatch.{Future, Http, url}

import scala.concurrent.ExecutionContext.Implicits.global

class FreeMarkerRendererSpec extends ExtensionSpec {

  val requests: List[(String, String, String, String)] = List(
    ("simple case", """{"single":"value"}""", s"$${$$.single}", "value"),
    ("fallback", """{}""", s"$${$$.single!1}", "1"),
    ("arithmetic", s"""{"value":3}""", s"$${$$.value + 3}", "6"),
    ("path as fallback", """{"single":"value"}""", s"$${$$.undefined!$$.single}", "value"),
    ("multi fallbacks", "{}", s"""$${$$.undefined!$$.undefined!"value"}""", "value"),
    ("array traversal", """{"array":["1","2"]}""", s"""[#ftl][#list $$.array as i]$${i}[/#list]""", """12"""),
    ("array glue", """{"array":["1","2"]}""", s"""$${$$.array?join(",")}""", """1,2"""),
    ("underscore", """{"_single":"value"}""", s"""{"single":"$${$$._single}"}""", """{"single":"value"}""")
  )

  "FreeMarkerRenderer" should "replace template response body" in {
    requests.foreach {
      case (clue, requestBody, responseBody, result) ⇒
        val requestUrl = "/" + UUID.randomUUID().toString

        stub("POST", requestUrl, responseBody, "freemarker-renderer") {
          val request: Future[Response] =
            Http(url(wireMockUrl + requestUrl)
              .<<(requestBody)
              .setContentType("application/json", "UTF-8"))

          validate(
            request = request,
            result = result,
            clue = "case [" + clue + "]"
          )
        }
    }
  }

}
