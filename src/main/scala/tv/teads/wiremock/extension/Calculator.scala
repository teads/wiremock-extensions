package tv.teads.wiremock.extension

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.{ResponseDefinition, Request}

import scala.util.Try
import scala.util.matching.Regex

class Calculator extends ResponseTransformer {

  override val name: String = "calculator"

  override val applyGlobally: Boolean = false

  val numberPattern: String = """([-+]?[0-9]*\.?[0-9]+)"""
  val operationPattern: String = """\s*(\+|-|\*|\/)\s*"""
  val pattern: Regex = (numberPattern + operationPattern + numberPattern).r

  override def transform(
    request:            Request,
    responseDefinition: ResponseDefinition,
    files:              FileSource
  ): ResponseDefinition = {
    Try {
      ResponseDefinitionBuilder
        .like(responseDefinition)
        .withBody(replaceCalculus(responseDefinition.getBody))
        .build()
    }.getOrElse(responseDefinition)
  }

  def replaceCalculus(source: String): String =
    replaceCalculusRec(source, "")

  def replaceCalculusRec(source: String, output: String): String = {
    findFirstCalculus(source) match {
      case None ⇒ output + source
      case Some(matched) ⇒
        val toAdd: String = Try {
          val a: BigDecimal = BigDecimal(matched.group(1))
          val b: BigDecimal = BigDecimal(matched.group(3))

          val result: BigDecimal = matched.group(2) match {
            case "+" ⇒ a + b
            case "-" ⇒ a - b
            case "*" ⇒ a * b
            case "/" ⇒ a / b
          }

          source.take(matched.start) + result.toString
        }.getOrElse(source.take(matched.end))

        replaceCalculusRec(source.drop(matched.end), output + toAdd)
    }
  }

  def findFirstCalculus(source: String): Option[Regex.Match] =
    pattern.findFirstMatchIn(source)

}
