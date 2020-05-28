package tv.teads.wiremock.extension

import java.util.UUID

import com.ning.http.client.Response
import dispatch.{Future, Http, url}

import scala.concurrent.ExecutionContext.Implicits.global

class FreeMarkerRendererSpec extends ExtensionSpec {

  val requests: List[(String, String, String, String)] = List(
    ("simple case", """{"single":"value"}""", s"$${$$.single}", "value"),
    ("fallback", """{}""", s"$${$$.single!1}", "1"),
    ("fallback for null nested path", """{}""", s"$${($$.single.value)!1}", "1"),
    ("arithmetic", s"""{"value":3}""", s"$${$$.value + 3}", "6"),
    ("path as fallback", """{"single":"value"}""", s"$${$$.undefined!$$.single}", "value"),
    ("multi fallbacks", "{}", s"""$${$$.undefined!$$.undefined!"value"}""", "value"),
    ("array traversal", """{"array":["1","2"]}""", s"""[#ftl][#list $$.array as i]$${i}[/#list]""", """12"""),
    ("array glue", """{"array":["1","2"]}""", s"""$${$$.array?join(",")}""", """1,2"""),
    ("array find first (simple case)", """{"cheap-cars":[{"details":{"price":15.5, "brand":"toyota"}},{"details":{"price":10,"brand":"lexus"}}]}""",
      s"""$${findFirstInArray('cheap-cars', 'details.brand == toyota', 'details.price')?c}""", """15.5"""),
    ("array find first (simple case 2)", """{"cheap-cars":[{"details":{"price":15.5, "brand":"toyota"}},{"details":{"price":10,"brand":"lexus"}}]}""",
      s"""$${findFirstInArray('cheap-cars', 'details.price == 15.5', 'details.brand')}""", """toyota"""),
    ("array find first (missing data)", """{"cheap-cars":[{"details":{"price":15.5, "brand":"toyota"}},{"details":{"price":10,"brand":"lexus"}}]}""",
      s"""$${(findFirstInArray('cheap-cars', 'details.brand == unknown', 'details.price')?c)!100}""", """100"""),
    ("array find first (array filter in wanted node)", """{"cheap-cars":[{"details":{"price":15.5,"brand":"toyota","customers":[{"name":"Alix","age":44},{"name":"Valentin","age":90}]}},{"details":{"price":10,"brand":"lexus","deals":[{"name":"Tristan","age":32}]}}]}""",
      s"""$${(findFirstInArray('cheap-cars', 'details.brand == toyota', 'details.customers[0].name'))!'defaultName'}""", """Alix"""),
    ("array find first (missing data / array filter in wanted node)", """{"cheap-cars":[{"details":{"price":15.5,"brand":"toyota","customers":[{"name":"Alix","age":44},{"name":"Valentin","age":90}]}},{"details":{"price":10,"brand":"lexus","deals":[{"name":"Tristan","age":32}]}}]}""",
      s"""$${(findFirstInArray('cheap-cars', 'details.brand == unknown', 'details.customers[0].name'))!'defaultName'}""", """defaultName"""),
    ("array find first (array filter in wanted node with invalid index)", """{"cheap-cars":[{"details":{"price":15.5,"brand":"toyota","customers":[{"name":"Alix","age":44},{"name":"Valentin","age":90}]}},{"details":{"price":10,"brand":"lexus","deals":[{"name":"Tristan","age":32}]}}]}""",
      s"""$${(findFirstInArray('cheap-cars', 'details.brand == toyota', 'details.customers[99].name'))!'defaultName'}""", """defaultName"""),
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
