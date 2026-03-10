package crawler.frontier

import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap
import scala.collection.mutable.PriorityQueue

class DomainPriorityQueue extends CrawlQueue {
  private val pq: PriorityQueue[(LocalDateTime, String)] =
    PriorityQueue.empty[(LocalDateTime, String)](Ordering[(LocalDateTime, String)].reverse)

  private val domainSet: java.util.Set[String] = ConcurrentHashMap.newKeySet()

  def addDomain(domain: String, nextCrawlableTime: LocalDateTime): Boolean = synchronized {
    if (domainSet.contains(domain)) return false
    domainSet.add(domain)
    pq.addOne((nextCrawlableTime, domain))
    true
  }

  def popAllCrawlableDomains(): List[String] = synchronized {
    var allDomains: List[String] = List.empty

    while (pq.nonEmpty && LocalDateTime.now().isAfter(pq.head._1)) {
      val poppedDomain = pq.dequeue()
      domainSet.remove(poppedDomain._2)
      allDomains = poppedDomain._2 :: allDomains
    }

    allDomains
  }

  def containsDomain(domain: String): Boolean = domainSet.contains(domain)
}
