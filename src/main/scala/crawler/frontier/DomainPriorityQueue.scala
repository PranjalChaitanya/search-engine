package crawler.frontier

import java.time.LocalDateTime
import scala.collection.mutable.PriorityQueue

object DomainPriorityQueue {
  val pq : PriorityQueue[(LocalDateTime, String)] =
    PriorityQueue.empty[(LocalDateTime, String)](Ordering[(LocalDateTime, String)].reverse)

  def addDomain(domain: String, nextCrawlableTime: LocalDateTime) = synchronized {
    pq.addOne((nextCrawlableTime, domain))
  }

  def popAllCrawlableDomains() : List[String] = synchronized {
    var allDomains : List[String] = List.empty
    
    while(pq.nonEmpty && LocalDateTime.now().isAfter(pq.head(0))) {
      val poppedDomain = pq.dequeue()
      allDomains = poppedDomain(1) :: allDomains
    }
    
    allDomains
  }
}
