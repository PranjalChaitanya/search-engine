package html

import crawler.html._
import crawler.html.HTMLObjectToken._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HTMLTokenizerSpec extends AnyFlatSpec with Matchers {

  // --------------------------------------------------
  // Helpers
  // --------------------------------------------------

  def text(tok: HTMLObjectToken): String = tok match {
    case TextToken(t) => t
    case other => fail(s"Expected TextToken but got $other")
  }

  def open(tok: HTMLObjectToken): OpenTagToken = tok match {
    case o: OpenTagToken => o
    case other => fail(s"Expected OpenTagToken but got $other")
  }

  def close(tok: HTMLObjectToken): CloseTagToken = tok match {
    case c: CloseTagToken => c
    case other => fail(s"Expected CloseTagToken but got $other")
  }

  def void(tok: HTMLObjectToken): VoidTagToken = tok match {
    case v: VoidTagToken => v
    case other => fail(s"Expected VoidTagToken but got $other")
  }

  // --------------------------------------------------
  // Plain text
  // --------------------------------------------------

  "HTMLTokenizer" should "tokenize plain text" in {
    val tokens = tokenize("hello world").toList

    tokens shouldBe List(
      TextToken("hello world")
    )
  }

  // --------------------------------------------------
  // Simple tags
  // --------------------------------------------------

  it should "tokenize a simple open/close tag pair" in {
    val tokens = tokenize("<p>hello</p>").toList

    tokens shouldBe List(
      OpenTagToken("p", Map.empty),
      TextToken("hello"),
      CloseTagToken("p")
    )
  }

  it should "preserve surrounding text" in {
    val tokens = tokenize("hi <p>there</p> friend").toList

    text(tokens(0)) shouldBe "hi "
    open(tokens(1)).tagName shouldBe "p"
    text(tokens(2)) shouldBe "there"
    close(tokens(3)).tagName shouldBe "p"
    text(tokens(4)) shouldBe " friend"
  }

  // --------------------------------------------------
  // Attributes
  // --------------------------------------------------

  it should "parse a single attribute" in {
    val tokens = tokenize("""<a href="x">y</a>""").toList

    val a = open(tokens.head)
    a.tagName shouldBe "a"
    a.attrs shouldBe Map("href" -> "x")
  }

  it should "parse multiple attributes" in {
    val tokens = tokenize(
      """<a href="x" class="y" id="z">text</a>"""
    ).toList

    val a = open(tokens.head)
    a.attrs shouldBe Map(
      "href" -> "x",
      "class" -> "y",
      "id" -> "z"
    )
  }

  // --------------------------------------------------
  // Void tags
  // --------------------------------------------------

  it should "parse void tags correctly" in {
    val tokens = tokenize("<br>").toList

    val br = void(tokens.head)
    br.tagName shouldBe "br"
    br.attrs shouldBe empty
  }

  it should "parse void tag with attributes" in {
    val tokens = tokenize("""<img src="a.png" />""").toList

    val img = void(tokens.head)
    img.tagName shouldBe "img"
    img.attrs shouldBe Map("src" -> "a.png")
  }

  // --------------------------------------------------
  // Comments
  // --------------------------------------------------

  it should "parse HTML comments" in {
    val tokens = tokenize("a <!-- hello --> b").toList

    tokens shouldBe List(
      TextToken("a "),
      CommentToken(" hello "),
      TextToken(" b")
    )
  }

  // --------------------------------------------------
  // Doctype
  // --------------------------------------------------

  it should "parse doctype" in {
    val tokens = tokenize("<!DOCTYPE html>").toList

    tokens shouldBe List(
      DoctypeToken()
    )
  }

  // --------------------------------------------------
  // Mixed content
  // --------------------------------------------------

  it should "parse mixed text, tags, and void tags" in {
    val html = "Hi <br> there <p>friend</p>"
    val tokens = tokenize(html).toList

    tokens shouldBe List(
      TextToken("Hi "),
      VoidTagToken("br", Map.empty),
      TextToken(" there "),
      OpenTagToken("p", Map.empty),
      TextToken("friend"),
      CloseTagToken("p")
    )
  }

  // --------------------------------------------------
  // Nested structure (token-level only)
  // --------------------------------------------------

  it should "tokenize nested tags in correct order" in {
    val tokens = tokenize("<div><p>x</p></div>").toList

    tokens.map(_.getClass) shouldBe List(
      classOf[OpenTagToken],
      classOf[OpenTagToken],
      classOf[TextToken],
      classOf[CloseTagToken],
      classOf[CloseTagToken]
    )

    open(tokens(0)).tagName shouldBe "div"
    open(tokens(1)).tagName shouldBe "p"
    close(tokens(3)).tagName shouldBe "p"
    close(tokens(4)).tagName shouldBe "div"
  }

  // --------------------------------------------------
  // Edge cases
  // --------------------------------------------------

  it should "not crash on malformed HTML" in {
    noException shouldBe thrownBy {
      tokenize("<p><</p>")
    }
  }

  it should "not crash on random garbage" in {
    noException shouldBe thrownBy {
      tokenize("<<<<<<>>>>>>")
    }
  }

  it should "not crash on unclosed tags" in {
    noException shouldBe thrownBy {
      tokenize("<html><p>hello</html>")
    }
  }

  // --------------------------------------------------
  // Realistic HTML
  // --------------------------------------------------

  it should "tokenize realistic HTML document" in {
    val html =
      "<!DOCTYPE html><html><body>Hello<br><p>World</p></body></html>"

    val tokens = tokenize(html).toList

    tokens.exists(_.isInstanceOf[DoctypeToken]) shouldBe true
    tokens.exists {
      case OpenTagToken("html", _) => true
      case _ => false
    } shouldBe true
    tokens.exists {
      case VoidTagToken("br", _) => true
      case _ => false
    } shouldBe true
  }

  // --------------------------------------------------
  // Stability
  // --------------------------------------------------

  it should "be deterministic" in {
    val html = "<p>a</p>"
    tokenize(html).toList shouldBe
      tokenize(html).toList
  }
}
