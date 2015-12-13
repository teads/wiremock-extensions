package tv.teads.wiremock.extension.json.extractor

import io.gatling.jsonpath.AST.{ArrayRandomAccess, RootNode}
import org.scalatest.{FlatSpec, Matchers}
import tv.teads.wiremock.extension.json.extractor.Ast.Expression.Value.Path
import tv.teads.wiremock.extension.json.extractor.Ast.Text

class ParserSpec extends FlatSpec with Matchers {

  "Parser" should "accept an empty string" in {
    Parser.parse(Parser.ast, "").get shouldEqual List()
  }

  it should "accept only text" in {
    Parser.parse(Parser.ast, "only text here").get shouldEqual List(Text("only text here"))
  }

  it should "parse a single path" in {
    Parser.parse(Parser.ast, s"$${$$[0]}").get shouldEqual List(Path(List(RootNode, ArrayRandomAccess(List(0)))))
  }

  it should "parse text and path" in {
    Parser.parse(Parser.ast, s"text $${$$[0]}").get shouldEqual List(
      Text("text "),
      Path(List(RootNode, ArrayRandomAccess(List(0))))
    )

    Parser.parse(Parser.ast, s"$${$$[0]} text").get shouldEqual List(
      Path(List(RootNode, ArrayRandomAccess(List(0)))),
      Text(" text")
    )

    Parser.parse(Parser.ast, s"$${$$[0]} text $${$$[0]}").get shouldEqual List(
      Path(List(RootNode, ArrayRandomAccess(List(0)))),
      Text(" text "),
      Path(List(RootNode, ArrayRandomAccess(List(0))))
    )

    Parser.parse(Parser.ast, s"text $${$$[0]} text").get shouldEqual List(
      Text("text "),
      Path(List(RootNode, ArrayRandomAccess(List(0)))),
      Text(" text")
    )
  }

}
