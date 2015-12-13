package tv.teads.wiremock.extension.json.extractor

import io.gatling.jsonpath.ParserBase
import tv.teads.wiremock.extension.json.extractor.Ast.Expression.Fallback
import tv.teads.wiremock.extension.json.extractor.Ast.Expression.Value.{Path, Const}
import tv.teads.wiremock.extension.json.extractor.Ast._

import scala.util.matching.Regex

object Parser extends ParserBase {

  // with wsString, whitespaces are not allowed within the start parser
  val start: Parser[Start.type] = wsString(Start.value) ^^ (_ ⇒ Start)
  val end: Parser[End.type] = End.value ^^ (_ ⇒ End)
  val or: Parser[Or.type] = Or.value ^^ (_ ⇒ Or)

  val const: Parser[Const] = """\w+""".r ^^ (value ⇒ Const(value))
  val path: Parser[Path] = (root ~ pathSequence) ^^ { case r ~ ps ⇒ Path(r :: ps) }

  val expr: Parser[Expression] = start ~ path ~ rep(or ~> path) ~ (or ~> const).? ~ end ^^ {
    case start ~ path ~ paths ~ const ~ end ⇒ construct(path, paths, const)
  }

  // with wsRegex, whitespaces are catch by the text parser
  val text: Parser[Text] = wsRegex(s""".+?(?=${Start.regex})|.+""".r) ^^ (value ⇒ Text(value))

  val ast: Parser[List[Ast]] = rep(expr | text)

  /**
   * Constructs an Expr tree from data.
   *
   * @param head the higher path
   * @param tail ordered possible fallback values
   *
   * @return a JsonPath with or without its fallbacks
   */
  def construct(
    head:  Path,
    tail:  List[Path],
    const: Option[Const]
  ): Expression = {
    (tail, const) match {
      case (Nil, None)        ⇒ head
      case (Nil, Some(const)) ⇒ Fallback(head, const)
      case (h :: t, _)        ⇒ Fallback(head, construct(h, t, const))
    }
  }

  /**
   * PARTS COPIED FROM REGEX PARSERS
   * TO ALLOW WHITE SPACES IN ONLY ONE PARSER
   */

  def wsString(s: String): Parser[String] = new Parser[String] {
    def apply(in: Input) = {
      val source = in.source
      val offset = in.offset
      val start = offset
      var i = 0
      var j = start
      while (i < s.length && j < source.length && s.charAt(i) == source.charAt(j)) {
        i += 1
        j += 1
      }
      if (i == s.length)
        Success(source.subSequence(start, j).toString, in.drop(j - offset))
      else {
        val found = if (start == source.length()) "end of source" else "`" + source.charAt(start) + "'"
        Failure("`" + s + "' expected but " + found + " found", in.drop(start - offset))
      }
    }
  }

  def wsRegex(r: Regex): Parser[String] = new Parser[String] {
    def apply(in: Input) = {
      val source = in.source
      val offset = in.offset
      val start = offset
      r.findPrefixMatchOf(new SubSequence(source, start)) match {
        case Some(matched) ⇒
          Success(
            source.subSequence(start, start + matched.end).toString,
            in.drop(start + matched.end - offset)
          )
        case None ⇒
          val found = if (start == source.length()) "end of source" else "`" + source.charAt(start) + "'"
          Failure("string matching regex `" + r + "' expected but " + found + " found", in.drop(start - offset))
      }
    }
  }

  class SubSequence(s: CharSequence, start: Int, val length: Int) extends CharSequence {
    def this(s: CharSequence, start: Int) = this(s, start, s.length - start)

    def charAt(i: Int) = {
      if (i >= 0 && i < length) s.charAt(start + i)
      else throw new IndexOutOfBoundsException("")
    }

    def subSequence(_start: Int, _end: Int) = {
      if (_start < 0 || _end < 0 || _end > length || _start > _end) {
        throw new IndexOutOfBoundsException("")
      }

      new SubSequence(s, start + _start, _end - _start)
    }
  }

}