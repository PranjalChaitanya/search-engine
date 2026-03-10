package frontier

import crawler.frontier.DomainPriorityQueue
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime
import java.util.concurrent.{CountDownLatch, Executors}
import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration._

class DomainPriorityQueueConcurrencyTest extends AnyFlatSpec with Matchers {

  it should "allow only one thread to successfully add the same domain under contention" in {
    val queue       = new DomainPriorityQueue()
    val threadCount = 20
    val latch       = new CountDownLatch(1)
    val successCount = new AtomicInteger(0)
    val pool        = Executors.newFixedThreadPool(threadCount)

    val tasks = (1 to threadCount).map { _ =>
      new Runnable {
        def run(): Unit = {
          latch.await()
          val added = queue.addDomain("example.com", LocalDateTime.now.plusSeconds(10))
          if (added) successCount.incrementAndGet()
        }
      }
    }

    tasks.foreach(pool.submit)
    latch.countDown()
    pool.shutdown()
    pool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)

    successCount.get() shouldBe 1
    queue.containsDomain("example.com") shouldBe true

    val popped = queue.popAllCrawlableDomains()
    popped.count(_ == "example.com") shouldBe 0  // not crawlable yet (future time)
  }

  it should "keep set and PQ consistent when many domains are added concurrently" in {
    val queue        = new DomainPriorityQueue()
    val threadCount  = 10
    val domainsPerThread = 5
    val pool         = Executors.newFixedThreadPool(threadCount)
    val latch        = new CountDownLatch(1)

    val tasks = (0 until threadCount).map { t =>
      new Runnable {
        def run(): Unit = {
          latch.await()
          (0 until domainsPerThread).foreach { d =>
            queue.addDomain(s"domain-$t-$d.com", LocalDateTime.now.minusSeconds(1))
          }
        }
      }
    }

    tasks.foreach(pool.submit)
    latch.countDown()
    pool.shutdown()
    pool.awaitTermination(5, java.util.concurrent.TimeUnit.SECONDS)

    val popped = queue.popAllCrawlableDomains()
    popped.length shouldBe threadCount * domainsPerThread
    popped.toSet.size shouldBe popped.length  // no duplicates
  }
}
