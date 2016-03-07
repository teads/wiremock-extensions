package tv.teads.wiremock.extension.calculator

import java.text.{DecimalFormat, DecimalFormatSymbols}

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.ResponseTransformer
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}
import net.objecthunter.exp4j.ExpressionBuilder

import scala.annotation.tailrec
import scala.util.Try
import scala.util.matching.Regex

class Calculator extends ResponseTransformer {

  override val name: String = "calculator"

  override val applyGlobally: Boolean = false

  private val pattern: Regex = """\$\{([ \d\+\-\*\/\(\)\.]+)}""".r

  private val separator: DecimalFormatSymbols = new DecimalFormatSymbols()
  separator.setDecimalSeparator('.')

  private val formatter: DecimalFormat = new DecimalFormat("0.#", separator)

  /**
   * Transforms a response's body by evaluating mathematical formulas.
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
      ResponseDefinitionBuilder
        .like(responseDefinition)
        .withBody(replaceCalculus(responseDefinition.getBody))
        .build()
    }.getOrElse(responseDefinition)
  }

  /**
   * Evaluates all formulas in the template which are encapsulated in ${...}
   *
   * @param template the response to transform
   */
  private def replaceCalculus(template: String): String = {

    @tailrec
    def rec(template: String, acc: String): String = {
      findFirstCalculus(template) match {
        case None ⇒ acc + template
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

          rec(template.drop(matched.end), acc + toAdd)
      }
    }

    rec(template, "")
  }

  /**
   * Finds the first mathematical formula in the template.
   */
  private def findFirstCalculus(template: String): Option[Regex.Match] =
    pattern.findFirstMatchIn(template)

}
