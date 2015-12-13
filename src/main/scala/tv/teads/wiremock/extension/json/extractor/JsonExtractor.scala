package tv.teads.wiremock.extension.json.extractor

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}
import io.gatling.jsonpath.AST.PathToken
import io.gatling.jsonpath.JsonPath
import tv.teads.wiremock.extension.json.extractor.Ast.Expression.Fallback
import tv.teads.wiremock.extension.json.extractor.Ast.Expression.Value.{Const, Path}
import tv.teads.wiremock.extension.json.extractor.Ast.Text

import scala.annotation.tailrec
import scala.util.Try

class JsonExtractor extends ResponseTransformer {

  override val name: String = "json-extractor"
  override val applyGlobally: Boolean = false
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
      val requestBody: Any = mapper.readValue(request.getBodyAsString, classOf[Object])
      val template: String = responseDefinition.getBody

      ResponseDefinitionBuilder
        .like(responseDefinition)
        .withBody(process(requestBody, template))
        .build()
    }.getOrElse(responseDefinition)
  }

  private def process(requestBody: Any, template: String): String = {

    @tailrec
    def rec(current: String, previous: String): String = {
      if (current.equals(previous)) previous
      else {
        val parsedTemplate: List[Ast] = parseResponseBody(current)
        val nextCurrent: String = render(requestBody, parsedTemplate)

        rec(nextCurrent, current)
      }
    }

    rec(template, "")
  }

  /**
   * Parses the responseBody into a typed AST.
   *
   * @param responseBody the response to transform
   */
  private def parseResponseBody(responseBody: String): List[Ast] = {
    Parser
      .parseAll(Parser.ast, responseBody)
      .getOrElse(List.empty)
  }

  /**
   * Renders a List of AST into a String using requestBody to get
   * data from JsonPaths.
   *
   * @param requestBody the requestBody holding data
   * @param parsedResponse the parsed response AST
   */
  private def render(requestBody: Any, parsedResponse: List[Ast]): String = {
    parsedResponse.foldLeft("") {
      case (responseBody, ast) ⇒ responseBody + stringify(requestBody, ast)
    }
  }

  /**
   * Stringify an AST using requestBody as data holder.
   *
   * @param requestBody the requestBody holding data
   * @param ast one element from the response
   */
  private def stringify(requestBody: Any, ast: Ast): String = ast match {
    case Text(value)              ⇒ value
    case Const(value)             ⇒ value
    case Path(ast)                ⇒ extractJsonPath(requestBody, ast).getOrElse("null")
    case Fallback(path, fallback) ⇒ extractJsonPath(requestBody, path.ast).getOrElse(stringify(requestBody, fallback))
  }

  private def extractJsonPath(requestBody: Any, ast: List[PathToken]): Option[String] = {
    new JsonPath(ast).query(requestBody).toList match {
      case Nil  ⇒ None
      case list ⇒ Option(list.map(_.toString).mkString)
    }
  }

}
