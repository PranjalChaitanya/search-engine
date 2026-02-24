package crawler.workflows

import crawler.core.{StepResult, Workflow, WorkflowContext, WorkflowStep, WorkflowSuccess, WorkflowTransition}
import crawler.scraper.scrapeWebpage

case object FetchWebpageStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    val scrapeResult : String = scrapeWebpage(input.ctx("webpage_url").toString)
    // TODO : Handle error cases. scrapeWebpage currently "never fails"
    input.ctx.put("scraped_result", scrapeResult)

    WorkflowSuccess
  }
}

case object TokenizeWebpageStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    WorkflowSuccess
  }
}

class CrawlPageWorkflow extends Workflow {
  override val startingStep: WorkflowStep = FetchWebpageStep
  
  override val transitions: Map[WorkflowStep, WorkflowTransition] = Map(
    FetchWebpageStep -> WorkflowTransition(
      onSuccess = Some(TokenizeWebpageStep),
      onFailure = None
    ),

    TokenizeWebpageStep -> WorkflowTransition(
      onSuccess = None, 
      onFailure = None
    )
  )
}
