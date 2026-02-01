package html

import crawler.html.*

import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.List

class HTMLURLExtractorTest extends AnyFlatSpec with Matchers {
  private def extractURLUtil(html: String): List[String] = extractURL(parseHTML(html))
  
  it should "extract url on single tag" in {
    val links: List[String] = extractURLUtil("<a href=\"https://gossim.com\">Link</a>")

    links shouldBe List("https://gossim.com")
  }

  it should "extract url on nested tag" in {
    val links: List[String] = extractURLUtil("<p><a href=\"https://gossim.com\">Link</a></p>")

    links shouldBe List("https://gossim.com")
  }

  it should "extract urls from basic general html" in {
    val html: String = """<!DOCTYPE html>
                         |<html lang="en">
                         |<head>
                         |</head>
                         |<body>
                         |  <nav>
                         |      <a href="https://example.com">Home</a>
                         |      <a href="https://example.com/about">About</a>
                         |      <a href="/contact">Contact</a>
                         |  </nav>
                         |
                         |  <main>
                         |    <a href="https://docs.example.com/api">API Docs</a>
                         |
                         |    <ul>
                         |        <li><a href="/help">Help</a></li>
                         |        <li><a href="/terms">Terms</a></li>
                         |    </ul>
                         |  </main>
                         |
                         |  <footer>
                         |    <a href="https://github.com/example/repo">Source</a>
                         |  </footer>
                         |
                         |</body>
                         |</html>
                         |""".stripMargin
    val links: List[String] = extractURLUtil(html)

    links shouldBe List("https://example.com", "https://example.com/about", "/contact", "https://docs.example.com/api",
      "/help", "/terms", "https://github.com/example/repo")
  }
}
