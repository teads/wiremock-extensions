package tv.teads.wiremock.extension

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}
import io.gatling.jsonpath.JsonPath

import scala.annotation.tailrec
import scala.util.Try
import scala.util.matching.Regex

class JsonExtractor extends ResponseTransformer {

  override val name: String = "json-extractor"

  override val applyGlobally: Boolean = false

  private val defaultRegex: Regex = """(?:\§(.+))?""".r
  private val jsonRegex: Regex = """(\$\.[ ='a-zA-Z0-9\@\.\[\]\*\,\:\?\(\)\&\|\<\>]*)""".r
  private val pattern: Regex = ("""\$\{""" + jsonRegex + defaultRegex + """\}""").r

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
    def rec(requestBody: Any, template: String, acc: String): String = {
      findFirstPath(template) match {
        case None ⇒ acc + template
        case Some(matched) ⇒
          val path: String = matched.group(1)
          val toAdd: String = extractValue(requestBody, path) match {
            case None ⇒
              // If there is a default value, use it
              // else just keep the raw template
              Option(matched.group(2)) match {
                case Some(default) ⇒ template.take(matched.start) + default
                case None          ⇒ template.take(matched.end)
              }
            case Some(value) ⇒
              // since we got a replacement
              // we will add it to the start of the matched path
              template.take(matched.start) + value
          }

          rec(requestBody, template.drop(matched.end), acc + toAdd)
      }
    }

    rec(requestBody, template, "")
  }

  /**
   * Finds the first JSONPath in the template.
   */
  private def findFirstPath(template: String): Option[Regex.Match] =
    pattern.findFirstMatchIn(template)

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
