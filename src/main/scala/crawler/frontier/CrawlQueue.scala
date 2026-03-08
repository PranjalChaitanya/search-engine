package crawler.frontier

import java.time.LocalDateTime

trait CrawlQueue {
  def addDomain(domain: String, nextCrawlableTime: LocalDateTime): Unit
  def popAllCrawlableDomains(): List[String]
}
