package crawler.workflows

import crawler.core.{StepResult, Workflow, WorkflowContext, WorkflowStep, WorkflowStepSuccess, WorkflowTransition}
import crawler.scraper.scrapeWebpage

case class FetchWebpage() extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    val scrapeResult : String = scrapeWebpage(input.ctx("webpage_url").toString)
    // TODO : Handle error cases. scrapeWebpage currently "never fails"
    input.ctx.put("scraped_result", scrapeResult)

    WorkflowStepSuccess("successful fetch")
  }
}

case class TokenizeWebpage() extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    ???
  }
}

case class FetchWebpageTransition() extends WorkflowTransition {
  override def next(result: StepResult): Option[WorkflowStep] = Some(TokenizeWebpage())
}

case class TokenizeWebpageTransition() extends WorkflowTransition {
  override def next(result: StepResult): Option[WorkflowStep] = ???
}

class CrawlPageWorkflow extends Workflow {
  override val startingStep: WorkflowStep = FetchWebpage()
  override val workflowSteps: List[(WorkflowStep, WorkflowTransition)] =
    List((FetchWebpage(), FetchWebpageTransition()), (TokenizeWebpage(), TokenizeWebpageTransition()))
}
