package tv.teads.wiremock.extension

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}
import io.gatling.jsonpath.FastStringOps.RichString
import io.gatling.jsonpath.JsonPath

import scala.annotation.tailrec
import scala.util.Try
import scala.util.matching.Regex

class JsonExtractor extends ResponseTransformer {

  case class Matched(all: String, path: String, fallback: Option[String])

  override val name: String = "json-extractor"

  override val applyGlobally: Boolean = false

  private val fallbackRegex: Regex = """(?:\§(.+?))?""".r
  private val jsonPathRegex: Regex = """(\$\.[ _='a-zA-Z0-9\@\.\[\]\*\,\:\?\(\)\&\|\<\>]*)""".r
  private val pattern: Regex = ("""\$\{""" + jsonPathRegex + fallbackRegex + """\}""").r

  private val mapper: ObjectMapper = new ObjectMapper

  /**
   * Transforms a response's body by extracting JSONPath and
   * replace them from the request.
   *
   * @param request a JSON request
   * @param responseDefinition the response to transform
   */
  override def transform(
    request:            Request,
    responseDefinition: ResponseDefinition,
    files:              FileSource
  ): ResponseDefinition = {
    Try {
      val requestBody = mapper.readValue(request.getBodyAsString, classOf[Object])
      val template = responseDefinition.getBody

      ResponseDefinitionBuilder
        .like(responseDefinition)
        .withBody(replacePaths(requestBody, template))
        .build()
    }.getOrElse(responseDefinition)
  }

  /**
   * Replaces all JSONPath in the template which are encapsulated in ${...}
   * by searching values in the requestBody.
   *
   * @param requestBody the JSON used to look for values
   * @param template the response to transform
   */
  private def replacePaths(requestBody: Any, template: String): String = {

    @tailrec
    def rec(requestBody: Any, current: String, previous: String): String = {
      if (current.equals(previous)) previous
      else {
        val nextCurrent: String =
          findAllPaths(current).foldLeft(current) {
            case (currentAcc, Matched(all, path, fallback)) ⇒
              extractValue(requestBody, path)
                .orElse(fallback)
                .map(currentAcc.fastReplaceAll(all, _))
                .getOrElse(currentAcc)
          }

        rec(requestBody, nextCurrent, current)
      }
    }

    rec(requestBody, template, "")
  }

  /**
   * Finds all JSONPaths in the template.
   */
  private def findAllPaths(template: String): Set[Matched] = {
    pattern.findAllMatchIn(template)
      .map(matched ⇒ Matched(matched.matched, matched.group(1), Option(matched.group(2))))
      .toSet
  }

  /**
   * Extracts the JSONPath value from the requestBody if any
   */
  private def extractValue(requestBody: Any, path: String): Option[String] = {
    JsonPath
      .query(path, requestBody)
      .right
      .map(_.toList.headOption.map(_.toString))
      .fold(_ ⇒ None, identity)
  }

}
