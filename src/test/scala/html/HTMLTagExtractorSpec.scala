package html

import crawler.html._
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class HTMLParserSpec extends AnyFlatSpec with Matchers {

  // --------------------------------------------------
  // Helper extractors (for readability)
  // --------------------------------------------------

  def nonVoid(node: DOMObject): DOMNonVoidTag =
    node match {
      case n: DOMNonVoidTag => n
      case other => fail(s"Expected DOMNonVoidTag but got $other")
    }

  def void(node: DOMObject): DOMVoidTag =
    node match {
      case v: DOMVoidTag => v
      case other => fail(s"Expected DOMVoidTag but got $other")
    }

  def text(node: DOMObject): String =
    node match {
      case DOMText(t) => t
      case other => fail(s"Expected DOMText but got $other")
    }

  // --------------------------------------------------
  // Basic correctness
  // --------------------------------------------------

  "parseHTML" should "parse plain text correctly" in {
    val html = "hello world"
    val result = HTMLParser.parseHTML(html)

    result shouldBe List(DOMText("hello world"))
  }

  it should "parse a simple non-void tag with correct children" in {
    val html = "<p>hello</p>"
    val result = HTMLParser.parseHTML(html)

    val p = nonVoid(result.head)
    p.tagType shouldBe "p"
    p.children shouldBe List(DOMText("hello"))
    p.attrs shouldBe empty
  }

  it should "preserve surrounding text" in {
    val html = "hi <p>there</p> friend"
    val result = HTMLParser.parseHTML(html)

    text(result(0)) shouldBe "hi "
    val p = nonVoid(result(1))
    text(result(2)) shouldBe " friend"

    p.children shouldBe List(DOMText("there"))
  }

  // --------------------------------------------------
  // Attributes
  // --------------------------------------------------

  it should "parse single attribute correctly" in {
    val html = """<a href="https://example.com">x</a>"""
    val result = HTMLParser.parseHTML(html)

    val a = nonVoid(result.head)
    a.tagType shouldBe "a"
    a.attrs shouldBe Map("href" -> "https://example.com")
  }

  it should "parse multiple attributes correctly" in {
    val html = """<a href="x" class="y">z</a>"""
    val result = HTMLParser.parseHTML(html)

    val a = nonVoid(result.head)
    a.attrs shouldBe Map(
      "href" -> "x",
      "class" -> "y"
    )
  }

  // --------------------------------------------------
  // Void tags
  // --------------------------------------------------

  it should "parse <br> as a void tag" in {
    val html = "<br>"
    val result = HTMLParser.parseHTML(html)

    val br = void(result.head)
    br.tagType shouldBe "br"
    br.attrs shouldBe empty
  }

  it should "parse <br /> as a void tag" in {
    val html = "<br />"
    val result = HTMLParser.parseHTML(html)

    val br = void(result.head)
    br.tagType shouldBe "br"
  }

  // --------------------------------------------------
  // Nesting correctness
  // --------------------------------------------------

  it should "parse nested tags with correct structure" in {
    val html = "<div><p>hello</p></div>"
    val result = HTMLParser.parseHTML(html)

    val div = nonVoid(result.head)
    div.tagType shouldBe "div"

    val p = nonVoid(div.children.head)
    p.tagType shouldBe "p"
    p.children shouldBe List(DOMText("hello"))
  }

  it should "parse multiple nested siblings correctly inside html root" in {
    val html = "<html><ul><li>a</li><li>b</li></ul></html>"
    val result = HTMLParser.parseHTML(html)

    val htmlTag = nonVoid(result.head)
    htmlTag.tagType shouldBe "html"

    val ul = nonVoid(htmlTag.children.head)
    ul.tagType shouldBe "ul"
    ul.children should have length 2

    val li1 = nonVoid(ul.children(0))
    val li2 = nonVoid(ul.children(1))

    text(li1.children.head) shouldBe "a"
    text(li2.children.head) shouldBe "b"
  }

  // --------------------------------------------------
  // Broken but documented edge cases
  // --------------------------------------------------

  it should "not crash on malformed html" in {
    val html = "<p><</p>"
    noException shouldBe thrownBy {
      HTMLParser.parseHTML(html)
    }
  }

  it should "not crash on unclosed tag" in {
    val html = "<html><p>hello</html>"
    noException shouldBe thrownBy {
      HTMLParser.parseHTML(html)
    }
  }

  it should "not crash on random input" in {
    val html = "<<<<<<<<>>>>>>>>>"
    noException shouldBe thrownBy {
      HTMLParser.parseHTML(html)
    }
  }

  // --------------------------------------------------
  // Realistic example (structure validation)
  // --------------------------------------------------

  it should "parse realistic HTML structure correctly" in {
    val html =
      "<html><br /> Hello <p>Basic <ul><li>Deep</li></ul> text</p></html>"

    val result = HTMLParser.parseHTML(html)

    val htmlTag = nonVoid(result.head)
    htmlTag.tagType shouldBe "html"

    val br = void(htmlTag.children(0))
    br.tagType shouldBe "br"

    text(htmlTag.children(1)) should include ("Hello")

    val p = nonVoid(htmlTag.children(2))
    p.children.exists {
      case DOMText(t) => t.contains("Basic")
      case _ => false
    } shouldBe true
  }
}