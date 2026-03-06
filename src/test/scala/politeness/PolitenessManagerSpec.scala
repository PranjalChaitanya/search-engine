package politeness

import crawler.politeness.PolitenessManager
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime

class PolitenessManagerSpec extends AnyFlatSpec with Matchers {

  // Reset shared state between tests so they don't bleed into each other.
  // PolitenessManager is a singleton, so we prime the cache manually rather
  // than relying on live HTTP.

  private val domain = "https://example.com"

  // ---- crawl delay / rate limiting ----

  "PolitenessManager.canCrawlNow" should "return true for a domain that has never been visited" in {
    // A fresh domain with no visit recorded is always crawlable
    val freshDomain = "https://never-visited-example.com"
    PolitenessManager.canCrawlNow(freshDomain) shouldBe true
  }

  it should "return false immediately after recording a visit" in {
    val d = "https://just-visited.example.com"
    PolitenessManager.recordVisit(d)
    // The crawl delay (at least 1 second) has not yet elapsed
    PolitenessManager.canCrawlNow(d) shouldBe false
  }

  "PolitenessManager.secondsUntilNextCrawl" should "return 0 for an unvisited domain" in {
    val d = "https://fresh-domain.example.com"
    PolitenessManager.secondsUntilNextCrawl(d) shouldBe 0L
  }

  it should "return a positive value immediately after a visit" in {
    val d = "https://rate-limit-test.example.com"
    PolitenessManager.recordVisit(d)
    PolitenessManager.secondsUntilNextCrawl(d) should be > 0L
  }

  // ---- nextAllowedCrawlTime ----

  "PolitenessManager.nextAllowedCrawlTime" should "be in the future after a visit" in {
    val d = "https://timing-test.example.com"
    PolitenessManager.recordVisit(d)
    PolitenessManager.nextAllowedCrawlTime(d).isAfter(LocalDateTime.now()) shouldBe true
  }

  it should "return now (or the past) for an unvisited domain" in {
    val d = "https://unvisited-timing.example.com"
    val result = PolitenessManager.nextAllowedCrawlTime(d)
    // Should be now or very slightly before (no delay accumulated)
    result.isBefore(LocalDateTime.now().plusSeconds(1)) shouldBe true
  }

  // ---- URL convenience overloads ----

  "PolitenessManager.recordVisitForURL / canCrawlURL" should "mirror the domain-level methods" in {
    val url = "https://url-overload-test.example.com/page"
    PolitenessManager.recordVisitForURL(url)
    PolitenessManager.canCrawlURL(url) shouldBe false
  }

  // ---- robots.txt cache invalidation ----

  "PolitenessManager.invalidateRobotsCache" should "not throw when the domain was never cached" in {
    noException shouldBe thrownBy {
      PolitenessManager.invalidateRobotsCache("https://uncached.example.com")
    }
  }

  // ---- defaultCrawlDelaySeconds ----

  "PolitenessManager.defaultCrawlDelaySeconds" should "be a positive integer" in {
    PolitenessManager.defaultCrawlDelaySeconds should be > 0
  }

  // ---- userAgent ----

  "PolitenessManager.userAgent" should "be non-empty" in {
    PolitenessManager.userAgent should not be empty
  }
}
