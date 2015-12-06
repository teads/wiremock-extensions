package tv.teads.wiremock.extension

import java.text.{DecimalFormatSymbols, DecimalFormat}

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.{ResponseDefinition, Request}
import net.objecthunter.exp4j.ExpressionBuilder

import scala.util.Try
import scala.util.matching.Regex

class Calculator extends ResponseTransformer {

  override val name: String = "calculator"

  override val applyGlobally: Boolean = false

  val pattern: Regex = """\$\{([ \d\+\-\*\/\(\)\.]+)}""".r

  val separator: DecimalFormatSymbols = new DecimalFormatSymbols()
  separator.setDecimalSeparator('.')

  val formatter: DecimalFormat = new DecimalFormat("0.#", separator)

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

  def replaceCalculus(template: String): String =
    replaceCalculusRec(template, "")

  def replaceCalculusRec(template: String, responseBody: String): String = {
    findFirstCalculus(template) match {
      case None ⇒ responseBody + template
      case Some(matched) ⇒
        val expression: String = matched.group(1)
        val toAdd: String = Try {
          val result = BigDecimal {
            new ExpressionBuilder(expression)
              .build()
              .evaluate()
              .toString
          }

          template.take(matched.start) + formatter.format(result)
        }.getOrElse(template.take(matched.end))

        replaceCalculusRec(template.drop(matched.end), responseBody + toAdd)
    }
  }

  def findFirstCalculus(template: String): Option[Regex.Match] =
    pattern.findFirstMatchIn(template)

}
