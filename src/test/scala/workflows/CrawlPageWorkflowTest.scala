package workflows

import crawler.core.{StepResult, WorkflowContext, WorkflowExecution, WorkflowStep, executeWorkflowStep}
import crawler.workflows.{FetchWebpageStep, TokenizeWebpageStep}
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
    val workflowExecution : WorkflowExecution = CrawlPageWorkflowFactory().createCrawlPageWorkflowExecution(url)

    executeWorkflowStep(workflowExecution)

    workflowExecution.workflowContext.ctx.contains("scraped_result") shouldBe true
    workflowExecution.currentState.contains(TokenizeWebpageStep) shouldBe true
  }
}
