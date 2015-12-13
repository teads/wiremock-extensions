package tv.teads.wiremock.extension.json.extractor

import io.gatling.jsonpath.AST

sealed trait Ast

object Ast {

  object Start { val value = s"$${"; val regex = """\$\{""" }
  object End { val value = "}" }
  object Or { val value = "|" }

  case class Text(value: String) extends Ast

  sealed trait Expression extends Ast
  object Expression {

    case class Fallback(path: Value.Path, fallback: Expression) extends Expression

    sealed trait Value extends Expression
    object Value {
      case class Const(value: String) extends Value
      case class Path(ast: List[AST.PathToken]) extends Value
    }

  }

}