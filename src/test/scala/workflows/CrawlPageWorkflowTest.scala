package workflows

import crawler.core.{StepResult, WorkflowContext, WorkflowExecution, WorkflowStep, executeEntireWorkflow, executeWorkflowStep}
import crawler.engine.ExecutionEngine
import crawler.workflows.{FetchWebpageStep, ParseWebpageStep}
import crawler.workflows.factories.CrawlPageWorkflowFactory
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

// Workflows are usually definitions, so testing workflows is usually not worthwhile. However, the purpose of this test
// is to test the core classes like Workflow and WorkflowExecution and all the related classes
class CrawlPageWorkflowTest extends AnyFlatSpec with Matchers {
  // Can't really test the contents of what you scrape because it changes
  it should "fetch a page and transition in success path" in {
    val workflowContext : WorkflowContext = WorkflowContext()

    workflowContext.ctx.put("webpage_url", "https://en.wikipedia.org/wiki/Apache_Iceberg")

    val outcomeResult: StepResult = FetchWebpageStep.run(workflowContext)

    workflowContext.ctx.contains("scraped_result") shouldBe true
    workflowContext.ctx("scraped_result").toString.nonEmpty shouldBe true
  }

  it should "properly execute a single step and create a workflow execution" in {
    val url : String = "https://en.wikipedia.org/wiki/Apache_Iceberg"
    val workflowExecution : WorkflowExecution =
      CrawlPageWorkflowFactory.createCrawlPageWorkflowExecution(url, ExecutionEngine(4))

    executeWorkflowStep(workflowExecution)

    workflowExecution.workflowContext.ctx.contains("scraped_result") shouldBe true
    workflowExecution.currentState.contains(ParseWebpageStep) shouldBe true
  }

  it should "complete full crawl workflow execution" in {
    val url : String = "https://en.wikipedia.org/wiki/Apache_Iceberg"
    val engine: ExecutionEngine = ExecutionEngine(4)

    val workflowExecution : WorkflowExecution =
      CrawlPageWorkflowFactory.createCrawlPageWorkflowExecution(url, engine)

    executeEntireWorkflow(workflowExecution)

    workflowExecution.workflowContext.ctx.contains("extracted_urls") shouldBe true

    // TODO : Chadnge this to use shouldBe also download an html page specifically to fetch
    assert(workflowExecution.workflowContext.ctx("extracted_urls").asInstanceOf[List[String]].length > 0)
  }
}
