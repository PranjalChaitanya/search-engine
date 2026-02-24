package workflows

import crawler.core.{StepResult, WorkflowContext, WorkflowExecution, WorkflowStep, WorkflowStepSuccess, executeWorkflowStep}
import crawler.workflows.factories.CrawlPageWorkflowFactory
import crawler.workflows.{FetchWebpage, FetchWebpageTransition, TokenizeWebpage}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.concurrent.TrieMap

// Workflows are usually definitions, so testing workflows is usually not worthwhile. However, the purpose of this test
// is to test the core classes like Workflow and WorkflowExecution and all the related classes
class CrawlPageWorkflowTest extends AnyFlatSpec with Matchers {
  it should "fetch a page and transition in success path" in {
    val workflowContext : WorkflowContext = WorkflowContext()

    workflowContext.ctx.put("webpage_url", "https://en.wikipedia.org/wiki/Apache_Iceberg")

    val outcomeResult: StepResult = FetchWebpage().run(workflowContext)

    assert(workflowContext.ctx.contains("scraped_result"))
    assert(workflowContext.ctx("scraped_result").toString.nonEmpty)

    assert(outcomeResult.isInstanceOf[WorkflowStepSuccess])

    val fetchWebpageTransition : FetchWebpageTransition = FetchWebpageTransition()
    val nextWorkflowStep : Option[WorkflowStep] =fetchWebpageTransition.next(outcomeResult)

    assert(nextWorkflowStep.isDefined)
    assert(nextWorkflowStep.get.isInstanceOf[TokenizeWebpage])
  }

  it should "properly execute a single step and create a workflow execution" in {
    val url : String = "https://en.wikipedia.org/wiki/Apache_Iceberg"
    val workflowExecution : WorkflowExecution = CrawlPageWorkflowFactory().createCrawlPageWorkflowExecution(url)

    executeWorkflowStep(workflowExecution)

    println("Step 1")
    println(workflowExecution.workflowContext.ctx)
    println(workflowExecution.workflowDefinition)
    println(workflowExecution.currentState)
    println(workflowExecution.isComplete)
    println("Step 2")
  }
}
