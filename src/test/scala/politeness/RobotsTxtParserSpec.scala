package politeness

import crawler.politeness.{RobotsTxtParser, RobotsTxtRules}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class RobotsTxtParserSpec extends AnyFlatSpec with Matchers {

  // ---- parse ----

  "RobotsTxtParser.parse" should "return empty rules for an empty file" in {
    val rules = RobotsTxtParser.parse("")
    rules.disallowedPaths shouldBe empty
    rules.allowedPaths    shouldBe empty
    rules.crawlDelaySeconds shouldBe None
  }

  it should "parse a wildcard Disallow correctly" in {
    val content =
      """|User-agent: *
         |Disallow: /private
         |""".stripMargin

    val rules = RobotsTxtParser.parse(content)
    rules.disallowedPaths should contain ("/private")
    rules.allowedPaths    shouldBe empty
  }

  it should "parse a wildcard Allow correctly" in {
    val content =
      """|User-agent: *
         |Allow: /public
         |Disallow: /
         |""".stripMargin

    val rules = RobotsTxtParser.parse(content)
    rules.allowedPaths    should contain ("/public")
    rules.disallowedPaths should contain ("/")
  }

  it should "parse Crawl-delay" in {
    val content =
      """|User-agent: *
         |Crawl-delay: 5
         |""".stripMargin

    RobotsTxtParser.parse(content).crawlDelaySeconds shouldBe Some(5)
  }

  it should "prefer an exact user-agent match over the wildcard group" in {
    val content =
      """|User-agent: *
         |Disallow: /wildcard-only
         |
         |User-agent: search-engine-bot
         |Disallow: /bot-specific
         |""".stripMargin

    val rules = RobotsTxtParser.parse(content)
    rules.disallowedPaths should contain     ("/bot-specific")
    rules.disallowedPaths should not contain ("/wildcard-only")
  }

  it should "fall back to the wildcard group when no exact match exists" in {
    val content =
      """|User-agent: *
         |Disallow: /wildcard-only
         |""".stripMargin

    val rules = RobotsTxtParser.parse(content)
    rules.disallowedPaths should contain ("/wildcard-only")
  }

  it should "ignore comment lines" in {
    val content =
      """|# This is a comment
         |User-agent: *
         |# Another comment
         |Disallow: /secret
         |""".stripMargin

    RobotsTxtParser.parse(content).disallowedPaths should contain ("/secret")
  }

  it should "strip inline comments from values" in {
    val content =
      """|User-agent: *
         |Disallow: /admin # private area
         |""".stripMargin

    RobotsTxtParser.parse(content).disallowedPaths should contain ("/admin")
  }

  it should "handle multiple groups correctly" in {
    val content =
      """|User-agent: googlebot
         |Disallow: /google-only
         |
         |User-agent: *
         |Disallow: /all
         |""".stripMargin

    val rules = RobotsTxtParser.parse(content)
    // Our bot matches the wildcard group
    rules.disallowedPaths should contain     ("/all")
    rules.disallowedPaths should not contain ("/google-only")
  }

  it should "treat a single Disallow with empty value as allow-all" in {
    val content =
      """|User-agent: *
         |Disallow:
         |""".stripMargin

    // Empty Disallow value means no path is disallowed
    RobotsTxtParser.parse(content).disallowedPaths shouldBe empty
  }

  // ---- isPathAllowed ----

  "RobotsTxtParser.isPathAllowed" should "allow any path when rules are empty" in {
    val rules = RobotsTxtRules(List.empty, List.empty, None)
    RobotsTxtParser.isPathAllowed("/anything", rules) shouldBe true
  }

  it should "disallow a path that matches a Disallow rule" in {
    val rules = RobotsTxtRules(List("/private"), List.empty, None)
    RobotsTxtParser.isPathAllowed("/private/page", rules) shouldBe false
  }

  it should "allow a path that does not match any Disallow rule" in {
    val rules = RobotsTxtRules(List("/private"), List.empty, None)
    RobotsTxtParser.isPathAllowed("/public/page", rules) shouldBe true
  }

  it should "let Allow override a less-specific Disallow" in {
    val rules = RobotsTxtRules(List("/private"), List("/private/ok"), None)
    RobotsTxtParser.isPathAllowed("/private/ok", rules) shouldBe true
    RobotsTxtParser.isPathAllowed("/private/secret", rules) shouldBe false
  }

  it should "disallow the root when Disallow is /" in {
    val rules = RobotsTxtRules(List("/"), List.empty, None)
    RobotsTxtParser.isPathAllowed("/anything", rules) shouldBe false
  }

  it should "match wildcard patterns" in {
    val rules = RobotsTxtRules(List("/api/*/internal"), List.empty, None)
    RobotsTxtParser.isPathAllowed("/api/v1/internal", rules) shouldBe false
    RobotsTxtParser.isPathAllowed("/api/v1/public",   rules) shouldBe true
  }

  it should "honour the $ end-anchor" in {
    val rules = RobotsTxtRules(List("/page$"), List.empty, None)
    RobotsTxtParser.isPathAllowed("/page",          rules) shouldBe false
    RobotsTxtParser.isPathAllowed("/page/subpage",  rules) shouldBe true  // not an exact match
  }

  it should "give Allow precedence over Disallow on equal specificity" in {
    // Same-length prefix — Allow should win
    val rules = RobotsTxtRules(List("/data"), List("/data"), None)
    RobotsTxtParser.isPathAllowed("/data/file", rules) shouldBe true
  }

  // ---- integration: parse then isPathAllowed ----

  "parse then isPathAllowed" should "work end-to-end for a realistic robots.txt" in {
    val content =
      """|User-agent: *
         |Disallow: /admin
         |Disallow: /tmp
         |Allow: /admin/public
         |Crawl-delay: 2
         |""".stripMargin

    val rules = RobotsTxtParser.parse(content)

    RobotsTxtParser.isPathAllowed("/",              rules) shouldBe true
    RobotsTxtParser.isPathAllowed("/articles",      rules) shouldBe true
    RobotsTxtParser.isPathAllowed("/admin",         rules) shouldBe false
    RobotsTxtParser.isPathAllowed("/admin/secret",  rules) shouldBe false
    RobotsTxtParser.isPathAllowed("/admin/public",  rules) shouldBe true
    RobotsTxtParser.isPathAllowed("/tmp/cache",     rules) shouldBe false

    rules.crawlDelaySeconds shouldBe Some(2)
  }
}
