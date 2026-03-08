package crawler.frontier

import java.time.LocalDateTime
import scala.collection.mutable.PriorityQueue

class DomainPriorityQueue extends CrawlQueue {
  private val pq: PriorityQueue[(LocalDateTime, String)] =
    PriorityQueue.empty[(LocalDateTime, String)](Ordering[(LocalDateTime, String)].reverse)

  def addDomain(domain: String, nextCrawlableTime: LocalDateTime): Unit = synchronized {
    pq.addOne((nextCrawlableTime, domain))
  }

  def popAllCrawlableDomains(): List[String] = synchronized {
    var allDomains: List[String] = List.empty

    while (pq.nonEmpty && LocalDateTime.now().isAfter(pq.head._1)) {
      val poppedDomain = pq.dequeue()
      allDomains = poppedDomain._2 :: allDomains
    }

    allDomains
  }
}

