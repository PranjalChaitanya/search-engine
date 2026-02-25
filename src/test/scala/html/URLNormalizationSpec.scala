package html

import crawler.html.{
  canonicalizeURL,
  createURLFromRelative,
  extractRootURL,
  isCrawlableLink,
  markURLAsSeen,
  normalizeAndFilterURLs
}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class URLNormalizationSpec extends AnyFlatSpec with Matchers {

  "extractRootURL" should "include explicit port when present" in {
    extractRootURL("https://example.com:8443/path/to/page") shouldBe "https://example.com:8443"
  }

  it should "exclude port when URL uses default host format" in {
    extractRootURL("https://example.com/path") shouldBe "https://example.com"
  }

  "createURLFromRelative" should "resolve dot segments correctly" in {
    createURLFromRelative("https://example.com/docs/guide/index.html", "../api") shouldBe "https://example.com/docs/api"
  }

  "canonicalizeURL" should "normalize host and scheme case, remove fragment, and trim trailing slash" in {
    canonicalizeURL("  HTTPS://Example.com/Docs/#section ") shouldBe Some("https://example.com/Docs")
  }

  it should "return None for invalid URLs" in {
    canonicalizeURL("http://exa mple.com") shouldBe None
  }

  "isCrawlableLink" should "reject non-http crawl targets" in {
    isCrawlableLink("javascript:void(0)") shouldBe false
    isCrawlableLink("mailto:test@example.com") shouldBe false
    isCrawlableLink("tel:+12345") shouldBe false
    isCrawlableLink("#fragment") shouldBe false
    isCrawlableLink("data:text/plain,hello") shouldBe false
  }

  it should "accept relative and absolute crawl targets" in {
    isCrawlableLink("/docs/start") shouldBe true
    isCrawlableLink("https://example.com/docs") shouldBe true
  }

  "markURLAsSeen" should "deduplicate repeated canonical URLs" in {
    val unique = java.util.UUID.randomUUID().toString
    val base = s"https://example.com/$unique"

    markURLAsSeen(base) shouldBe true
    markURLAsSeen(s"$base/") shouldBe false
  }

  "normalizeAndFilterURLs" should "normalize, filter and deduplicate links in-order" in {
    val unique = java.util.UUID.randomUUID().toString
    val baseURL = s"https://example.com/root/$unique/index.html"

    val normalized = normalizeAndFilterURLs(
      List(
        " /about ",
        "/about/",              // duplicate after canonicalization
        "#section",             // filtered
        "javascript:void(0)",   // filtered
        s"https://EXAMPLE.com/root/$unique/contact#top",
        "https://example.com/root/$unique/contact", // duplicate canonical
        "mailto:test@example.com",                  // filtered
        "https://another.example.org/path"
      ),
      baseURL
    )

    normalized shouldBe List(
      "https://example.com/about",
      s"https://example.com/root/$unique/contact",
      "https://another.example.org/path"
    )
  }
}
