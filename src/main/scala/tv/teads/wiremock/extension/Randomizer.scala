package tv.teads.wiremock.extension

import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.common.FileSource
import com.github.tomakehurst.wiremock.extension.{Parameters, ResponseDefinitionTransformer}
import com.github.tomakehurst.wiremock.http.{Request, ResponseDefinition}

import scala.annotation.tailrec
import scala.util.{Random, Try}
import scala.util.matching.Regex

class Randomizer extends ResponseDefinitionTransformer {

  override def getName: String = "randomizer"

  override val applyGlobally: Boolean = false

  private val randomPattern: Regex = """\$\{([Random\w]+)}""".r

  /**
   * Transforms a response's body by generating random values.
   *
   * @param request            a JSON request
   * @param responseDefinition the response to transform
   */
  override def transform(
    request:            Request,
    responseDefinition: ResponseDefinition,
    files:              FileSource,
    parameters:         Parameters
  ): ResponseDefinition = Try {
    ResponseDefinitionBuilder
      .like(responseDefinition)
      .withBody(replaceRandom(responseDefinition.getBody))
      .build()
  }.getOrElse(responseDefinition)

  /**
   * Evaluates all random placeholders in the template which are encapsulated in ${RandomXXX}
   *
   * @param template the response to transform
   */
  private def replaceRandom(template: String): String = {

    @tailrec
    def rec(template: String, acc: String): String = {
      findFirstRandomPlaceholder(template) match {
        case None ⇒ acc + template
        case Some(matched) ⇒
          val expression: String = matched.group(1)
          val result: String = expression match {
            case "RandomInteger" ⇒ String.valueOf(new Random().nextInt(Integer.MAX_VALUE))
            case "RandomDouble"  ⇒ String.valueOf(new Random().nextDouble())
            case "RandomBoolean" ⇒ String.valueOf(new Random().nextBoolean())
            case "RandomFloat"   ⇒ String.valueOf(new Random().nextFloat())
            case "RandomLong"    ⇒ String.valueOf(Math.abs(new Random().nextLong()))
            case "RandomString"  ⇒ Random.alphanumeric.take(10).mkString
            case _               ⇒ acc + template
          }
          val toAdd: String = template.take(matched.start) + result

          rec(template.drop(matched.end), acc + toAdd)
      }
    }

    rec(template, acc = "")
  }

  /**
   * Finds the first random placeholder in the template.
   */
  private def findFirstRandomPlaceholder(template: String): Option[Regex.Match] =
    randomPattern.findFirstMatchIn(template)

}
