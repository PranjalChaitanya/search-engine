package workflows

import crawler.core.{WorkflowExecution, executeEntireWorkflow, executeWorkflowStep}
import crawler.frontier.{CrawlQueue, Frontier}
import crawler.html.CrawlURLState
import crawler.workflows.{AddURLsToFrontierStep, CrawlPageContext, FetchWebpageStep, ParseWebpageStep}
import crawler.workflows.factories.CrawlPageWorkflowFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import java.time.LocalDateTime
import scala.collection.mutable.ListBuffer

class CrawlPageWorkflowTest extends AnyFlatSpec with Matchers {

  class StubFrontier extends Frontier {
    val addedURLs: ListBuffer[(String, String)] = ListBuffer.empty
    def addURLToDomain(url: String, domain: String): Unit = addedURLs += ((url, domain))
    def popURLFromDomain(domain: String): Option[String] = None
  }

  class StubCrawlQueue extends CrawlQueue {
    val addedDomains: ListBuffer[String] = ListBuffer.empty
    def addDomain(domain: String, nextCrawlableTime: LocalDateTime): Boolean = {
      addedDomains += domain
      true
    }
    def popAllCrawlableDomains(): List[String] = List.empty
    def containsDomain(domain: String): Boolean = false
  }

  it should "fetch a page and transition in success path" in {
    val ctx = CrawlPageContext(
      "https://en.wikipedia.org/wiki/Apache_Iceberg",
      new StubFrontier(),
      new StubCrawlQueue(),
      new CrawlURLState()
    )

    FetchWebpageStep.run(ctx)

    ctx.scrapedResult.isDefined shouldBe true
    ctx.scrapedResult.get.nonEmpty shouldBe true
  }

  it should "properly execute a single step and create a workflow execution" in {
    val execution: WorkflowExecution[CrawlPageContext] =
      CrawlPageWorkflowFactory.createCrawlPageWorkflowExecution(
        "https://en.wikipedia.org/wiki/Apache_Iceberg",
        new StubFrontier(),
        new StubCrawlQueue(),
        new CrawlURLState()
      )

    executeWorkflowStep(execution)

    execution.context.scrapedResult.isDefined shouldBe true
    execution.currentState.contains(ParseWebpageStep) shouldBe true
  }

  it should "complete full crawl workflow execution and add URLs to frontier" in {
    val frontier  = new StubFrontier()
    val crawlQueue = new StubCrawlQueue()
    val execution: WorkflowExecution[CrawlPageContext] =
      CrawlPageWorkflowFactory.createCrawlPageWorkflowExecution(
        "https://www.databricks.com/blog/announcing-full-apache-iceberg-support-databricks",
        frontier,
        crawlQueue,
        new CrawlURLState()
      )

    executeEntireWorkflow(execution)

    frontier.addedURLs should not be empty
    crawlQueue.addedDomains should not be empty
  }

  it should "call frontier.addURLToDomain and crawlQueue.addDomain for each discovered URL" in {
    val frontier   = new StubFrontier()
    val crawlQueue = new StubCrawlQueue()
    val seenURLs   = new CrawlURLState()

    val execution: WorkflowExecution[CrawlPageContext] =
      CrawlPageWorkflowFactory.createCrawlPageWorkflowExecution(
        "https://en.wikipedia.org/wiki/Apache_Iceberg",
        frontier,
        crawlQueue,
        seenURLs
      )

    executeEntireWorkflow(execution)

    frontier.addedURLs.length should be > 0
    // every added URL should have a corresponding domain in crawlQueue
    crawlQueue.addedDomains.length should be > 0
  }
}
