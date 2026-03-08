package workflows

import crawler.core.{WorkflowExecution, executeEntireWorkflow, executeWorkflowStep}
import crawler.engine.ExecutionEngine
import crawler.workflows.{CrawlPageContext, FetchWebpageStep, ParseWebpageStep}
import crawler.workflows.factories.CrawlPageWorkflowFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

// Workflows are usually definitions, so testing workflows is usually not worthwhile. However, the purpose of this test
// is to test the core classes like Workflow and WorkflowExecution and all the related classes
class CrawlPageWorkflowTest extends AnyFlatSpec with Matchers {

  it should "fetch a page and transition in success path" in {
    val ctx = CrawlPageContext("https://en.wikipedia.org/wiki/Apache_Iceberg", ExecutionEngine(4))

    val result = FetchWebpageStep.run(ctx)

    ctx.scrapedResult.isDefined shouldBe true
    ctx.scrapedResult.get.nonEmpty shouldBe true
  }

  it should "properly execute a single step and create a workflow execution" in {
    val url = "https://en.wikipedia.org/wiki/Apache_Iceberg"
    val execution: WorkflowExecution[CrawlPageContext] =
      CrawlPageWorkflowFactory.createCrawlPageWorkflowExecution(url, ExecutionEngine(4))

    executeWorkflowStep(execution)

    execution.context.scrapedResult.isDefined shouldBe true
    execution.currentState.contains(ParseWebpageStep) shouldBe true
  }

  it should "complete full crawl workflow execution" in {
    val url = "https://www.databricks.com/blog/announcing-full-apache-iceberg-support-databricks"
    val execution: WorkflowExecution[CrawlPageContext] =
      CrawlPageWorkflowFactory.createCrawlPageWorkflowExecution(url, ExecutionEngine(4))

    executeEntireWorkflow(execution)

    execution.context.extractedUrls.isDefined shouldBe true
    execution.context.extractedUrls.get.length should be > 0
  }
}
