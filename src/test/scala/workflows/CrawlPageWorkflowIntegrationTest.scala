package workflows

import crawler.core.Config
import crawler.engine.ExecutionEngine
import crawler.frontier.{DomainFrontier, DomainPriorityQueue}
import crawler.html.{CrawlURLState, extractRootURL}
import crawler.workflows.factories.CrawlPageWorkflowFactory.createCrawlPageWorkflowExecutionCallback
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime

class CrawlPageWorkflowIntegrationTest extends AnyFlatSpec with Matchers {
  val url: String       = "https://en.wikipedia.org/wiki/Apache_Iceberg"
  val frontier          = new DomainFrontier()
  val crawlQueue        = new DomainPriorityQueue()
  val seenURLs          = new CrawlURLState()
  val engine            = ExecutionEngine(Config.executionEngineExecutorThreads)

  engine.start()
  engine.submitJob(createCrawlPageWorkflowExecutionCallback(url, frontier, crawlQueue, seenURLs))

  Thread.sleep(10000)

  engine.shutdown()

  it should "populate the frontier with discovered URLs after crawling a page" in {
    val domain = extractRootURL(url)
    // frontier should have received URLs from the crawled page
    frontier.popURLFromDomain(domain).isDefined shouldBe true
  }
}
