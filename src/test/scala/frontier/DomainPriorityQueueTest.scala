package frontier

import crawler.frontier.DomainPriorityQueue
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime

class DomainPriorityQueueTest extends AnyFlatSpec with Matchers {
  it should "correctly pop all crawlable domains and exclude non crawlable domains" in {
    // These domains should be crawlable
    DomainPriorityQueue.addDomain("domain1", LocalDateTime.now.minusSeconds(1))
    DomainPriorityQueue.addDomain("domain2", LocalDateTime.now.minusHours(1))
    DomainPriorityQueue.addDomain("domain3", LocalDateTime.now.minusDays(1))
    DomainPriorityQueue.addDomain("domain4", LocalDateTime.now.minusYears(1))

    // These domains should not be crawlable
    DomainPriorityQueue.addDomain("domain5", LocalDateTime.now.plusSeconds(1))
    DomainPriorityQueue.addDomain("domain6", LocalDateTime.now.plusHours(1))
    DomainPriorityQueue.addDomain("domain7", LocalDateTime.now.plusDays(1))
    DomainPriorityQueue.addDomain("domain8", LocalDateTime.now.plusYears(1))

    val crawlableDomains : List[String] = DomainPriorityQueue.popAllCrawlableDomains()

    crawlableDomains.length shouldBe 4

    crawlableDomains.contains("domain1") shouldBe true
    crawlableDomains.contains("domain2") shouldBe true
    crawlableDomains.contains("domain3") shouldBe true
    crawlableDomains.contains("domain4") shouldBe true
  }
}
