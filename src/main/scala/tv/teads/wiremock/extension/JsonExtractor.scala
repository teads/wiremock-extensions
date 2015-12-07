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

  val pattern: Regex = """\$\{(\$\.[ ='a-zA-Z0-9\@\.\[\]\*\,\:\?\(\)]*)\}""".r

  val mapper: ObjectMapper = new ObjectMapper

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
   * Replaces all JSONPath from template by searching values
   * in the requestBody.
   *
   * @param requestBody the JSON used to look for values
   * @param template the response to transform
   * @return
   */
  def replacePaths(requestBody: Any, template: String): String =
    replacePathsRec(requestBody, template, "")

  @tailrec
  private def replacePathsRec(requestBody: Any, template: String, responseBody: String): String = {
    findFirstPath(template) match {
      case None ⇒ responseBody + template
      case Some(matched) ⇒
        val path: String = matched.group(1)
        val toAdd: String = extractValue(requestBody, path) match {
          case None ⇒
            // since we don't have anything to replace
            // we will add the raw template to the output body
            template.take(matched.end)
          case Some(value) ⇒
            // since we got a replacement
            // we will add it to the start of the matched path
            template.take(matched.start) + value
        }

        replacePathsRec(requestBody, template.drop(matched.end), responseBody + toAdd)
    }
  }

  /**
   * Finds the first JSONPath in the template.
   */
  def findFirstPath(template: String): Option[Regex.Match] =
    pattern.findFirstMatchIn(template)

  /**
   * Extracts the JSONPath value from the requestBody if any.
   */
  def extractValue(requestBody: Any, path: String): Option[String] = {
    JsonPath
      .query(path, requestBody)
      .right
      .map(_.toList.headOption.map(_.toString))
      .fold(_ ⇒ None, identity)
  }

}
