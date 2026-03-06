package crawler.politeness

import crawler.core.Logger
import crawler.html.extractRootURL
import crawler.scraper.scrapeWebpage

import java.net.URI
import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

/**
 * Stateful manager for crawler politeness.
 *
 * Responsibilities:
 *   - Fetching and caching robots.txt per domain
 *   - Checking whether a URL is allowed by robots.txt
 *   - Enforcing per-domain crawl delays (from robots.txt or a sensible default)
 *   - Tracking the last visit time per domain so callers can gate crawls
 */
object PolitenessManager {

  /** Delay to use when robots.txt provides no Crawl-delay directive (seconds). */
  val defaultCrawlDelaySeconds: Int = 1

  val userAgent: String = RobotsTxtParser.crawlerUserAgent

  // domain root (e.g. "https://example.com") -> parsed rules
  private val robotsCache   = new ConcurrentHashMap[String, RobotsTxtRules]()

  // domain root -> time of last successful fetch from that domain
  private val lastVisitTime = new ConcurrentHashMap[String, LocalDateTime]()

  // ---- robots.txt ----

  /**
   * Fetch, parse, and cache robots.txt for `domainRoot` (e.g. "https://example.com").
   * If the file cannot be fetched, all paths are treated as allowed.
   */
  def fetchAndCacheRobotsTxt(domainRoot: String): RobotsTxtRules = {
    val robotsUrl = s"$domainRoot/robots.txt"
    Logger.log(s"PolitenessManager: fetching robots.txt from $robotsUrl")

    val rules = scrapeWebpage(robotsUrl) match {
      case Some(content) =>
        val parsed = RobotsTxtParser.parse(content)
        Logger.log(s"PolitenessManager: parsed robots.txt for $domainRoot — " +
          s"${parsed.disallowedPaths.size} disallow rule(s), " +
          s"crawl-delay=${parsed.crawlDelaySeconds.getOrElse(defaultCrawlDelaySeconds)}s")
        parsed
      case None =>
        Logger.log(s"PolitenessManager: could not fetch robots.txt for $domainRoot, allowing all paths")
        RobotsTxtRules(List.empty, List.empty, None)
    }

    robotsCache.put(domainRoot, rules)
    rules
  }

  /** Return cached rules for `domainRoot`, fetching them first if not yet cached. */
  def rulesFor(domainRoot: String): RobotsTxtRules =
    Option(robotsCache.get(domainRoot)).getOrElse(fetchAndCacheRobotsTxt(domainRoot))

  /** Drop cached robots.txt for `domainRoot` so it is re-fetched on the next check. */
  def invalidateRobotsCache(domainRoot: String): Unit = {
    robotsCache.remove(domainRoot)
    Logger.log(s"PolitenessManager: robots.txt cache invalidated for $domainRoot")
  }

  // ---- URL allowance ----

  /**
   * Return true when `url` is permitted by the robots.txt rules of its domain.
   * Fetches and caches robots.txt if not already cached.
   */
  def isAllowed(url: String): Boolean = {
    val domainRoot = extractRootURL(url)
    val rules      = rulesFor(domainRoot)
    val path       = extractPathWithQuery(url)
    RobotsTxtParser.isPathAllowed(path, rules)
  }

  // ---- crawl-delay / rate limiting ----

  /** Crawl delay in seconds for `domainRoot` (robots.txt value, or the default). */
  def crawlDelayFor(domainRoot: String): Int =
    rulesFor(domainRoot).crawlDelaySeconds.getOrElse(defaultCrawlDelaySeconds)

  /**
   * Record that we just issued a request to `domainRoot`.
   * Should be called immediately before (or after) each fetch.
   */
  def recordVisit(domainRoot: String): Unit = {
    lastVisitTime.put(domainRoot, LocalDateTime.now())
  }

  /**
   * The earliest time at which we are permitted to send another request to
   * `domainRoot`. Returns `now` when the domain has never been visited.
   */
  def nextAllowedCrawlTime(domainRoot: String): LocalDateTime =
    Option(lastVisitTime.get(domainRoot)) match {
      case None       => LocalDateTime.MIN  // never visited → always crawlable
      case Some(last) => last.plusSeconds(crawlDelayFor(domainRoot))
    }

  /** True when enough time has elapsed since the last visit to `domainRoot`. */
  def canCrawlNow(domainRoot: String): Boolean = {
    val now  = LocalDateTime.now()
    val next = nextAllowedCrawlTime(domainRoot)
    !now.isBefore(next)
  }

  /**
   * How many seconds remain until `domainRoot` can be crawled again.
   * Returns 0 when the domain is already crawlable.
   */
  def secondsUntilNextCrawl(domainRoot: String): Long = {
    val now  = LocalDateTime.now()
    val next = nextAllowedCrawlTime(domainRoot)
    if (!next.isAfter(now)) 0L
    else (java.time.Duration.between(now, next).toMillis + 999) / 1000  // ceiling division
  }

  // ---- convenience overloads that accept a full URL instead of a domain root ----

  /** Like `crawlDelayFor` but derives the domain root from `url`. */
  def crawlDelayForURL(url: String): Int =
    crawlDelayFor(extractRootURL(url))

  /** Like `recordVisit` but derives the domain root from `url`. */
  def recordVisitForURL(url: String): Unit =
    recordVisit(extractRootURL(url))

  /** Like `canCrawlNow` but derives the domain root from `url`. */
  def canCrawlURL(url: String): Boolean =
    canCrawlNow(extractRootURL(url))

  /** Like `nextAllowedCrawlTime` but derives the domain root from `url`. */
  def nextAllowedCrawlTimeForURL(url: String): LocalDateTime =
    nextAllowedCrawlTime(extractRootURL(url))

  // ---- private helpers ----

  private def extractPathWithQuery(url: String): String =
    try {
      val uri   = new URI(url)
      val path  = Option(uri.getPath).filter(_.nonEmpty).getOrElse("/")
      val query = Option(uri.getQuery).map("?" + _).getOrElse("")
      path + query
    } catch {
      case _: Exception => "/"
    }
}
