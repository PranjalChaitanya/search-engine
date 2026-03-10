package crawler.frontier

import java.time.LocalDateTime

trait CrawlQueue {
  def addDomain(domain: String, nextCrawlableTime: LocalDateTime): Boolean
  def popAllCrawlableDomains(): List[String]
  def containsDomain(domain: String): Boolean
}
