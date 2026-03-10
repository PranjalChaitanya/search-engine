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

  it should "return true on first addDomain and false on duplicate" in {
    val queue = new DomainPriorityQueue()

    queue.addDomain("example.com", LocalDateTime.now.plusSeconds(10)) shouldBe true
    queue.addDomain("example.com", LocalDateTime.now.plusSeconds(10)) shouldBe false
  }

  it should "not double-enqueue a domain when added twice" in {
    val queue = new DomainPriorityQueue()

    queue.addDomain("example.com", LocalDateTime.now.minusSeconds(1))
    queue.addDomain("example.com", LocalDateTime.now.minusSeconds(1))

    val popped = queue.popAllCrawlableDomains()
    popped should have length 1
    popped should contain ("example.com")
  }

  it should "allow re-adding a domain after it has been popped" in {
    val queue = new DomainPriorityQueue()

    queue.addDomain("example.com", LocalDateTime.now.minusSeconds(1)) shouldBe true
    val first = queue.popAllCrawlableDomains()
    first should contain ("example.com")

    queue.addDomain("example.com", LocalDateTime.now.minusSeconds(1)) shouldBe true
    val second = queue.popAllCrawlableDomains()
    second should contain ("example.com")
  }

  it should "report containsDomain correctly before and after add and pop" in {
    val queue = new DomainPriorityQueue()

    queue.containsDomain("example.com") shouldBe false

    queue.addDomain("example.com", LocalDateTime.now.minusSeconds(1))
    queue.containsDomain("example.com") shouldBe true

    queue.popAllCrawlableDomains()
    queue.containsDomain("example.com") shouldBe false
  }
}
