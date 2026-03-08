package frontier

import crawler.frontier.DomainPriorityQueue
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime

class DomainPriorityQueueTest extends AnyFlatSpec with Matchers {
  it should "correctly pop all crawlable domains and exclude non crawlable domains" in {
    val queue = new DomainPriorityQueue()

    queue.addDomain("domain1", LocalDateTime.now.minusSeconds(1))
    queue.addDomain("domain2", LocalDateTime.now.minusHours(1))
    queue.addDomain("domain3", LocalDateTime.now.minusDays(1))
    queue.addDomain("domain4", LocalDateTime.now.minusYears(1))

    queue.addDomain("domain5", LocalDateTime.now.plusSeconds(1))
    queue.addDomain("domain6", LocalDateTime.now.plusHours(1))
    queue.addDomain("domain7", LocalDateTime.now.plusDays(1))
    queue.addDomain("domain8", LocalDateTime.now.plusYears(1))

    val crawlableDomains = queue.popAllCrawlableDomains()

    crawlableDomains.length shouldBe 4
    crawlableDomains should contain allOf ("domain1", "domain2", "domain3", "domain4")
  }
}
