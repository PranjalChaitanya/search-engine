package workflows

import crawler.core.Config
import crawler.engine.ExecutionEngine
import crawler.frontier.{DomainFrontier, DomainPriorityQueue}
import crawler.html.{SeenURLStore, extractRootURL}
import crawler.scheduler.factories.SchedulerFactory
import org.scalatest.{Tag}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

object IntegrationTest extends Tag("IntegrationTest")

import java.time.LocalDateTime
import java.util.concurrent.ConcurrentHashMap

class CrawlPageWorkflowIntegrationTest extends AnyFlatSpec with Matchers {

  class CountingURLState extends SeenURLStore {
    private val seen = ConcurrentHashMap.newKeySet[String]()
    def markIfNew(url: String): Boolean = seen.add(url)
    def size: Int = seen.size()
  }

  val seedUrl   = "https://en.wikipedia.org/wiki/Apache_Iceberg"
  val frontier  = new DomainFrontier()
  val crawlQueue = new DomainPriorityQueue()
  val seenURLs  = new CountingURLState()
  val engine    = new ExecutionEngine(Config.executionEngineNormalThreads, Config.executionEngineUrgentThreads)

  val domain = extractRootURL(seedUrl)
  frontier.addURLToDomain(seedUrl, domain)
  crawlQueue.addDomain(domain, LocalDateTime.MIN)

  val scheduler = SchedulerFactory.createScheduler(frontier, crawlQueue, seenURLs, engine)

  engine.start()
  scheduler.start()

  Thread.sleep(25000)

  scheduler.stop()
  engine.shutdown()

  it should "crawl more than one URL when the full scheduler pipeline is running" taggedAs IntegrationTest in {
    seenURLs.size should be > 1
  }
}
