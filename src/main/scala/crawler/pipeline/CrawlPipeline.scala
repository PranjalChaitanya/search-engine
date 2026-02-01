package crawler.pipeline

import crawler.core.{State, StateMachine, Step, Workflow, WorkflowContext, WorkflowStep}
import crawler.html.{DOMObject, HTMLObjectToken, extractURL, parseHTML, tokenize}
import crawler.scraper.scrapeWebpage

final case class CrawlPipelineWorkflowContext() extends WorkflowContext

private def executeAndMoveStateCrawlPipeline(stateInput: WorkflowContext): Step  = {
  ???
}

private def getCrawlPipelineStateMachine(url: String): StateMachine[Step, WorkflowContext] = {
  class FetchState extends WorkflowStep(scrapeWebpage)
  class TokenizeState extends WorkflowStep(tokenize)
  class ParseState extends WorkflowStep(parseHTML)
  class ExtractURLState extends WorkflowStep(extractURL)

  val states: List[Step] = List(FetchState(), TokenizeState(), ParseState(), ExtractURLState())
  
  
  // TODO : This fetch state is different from the one already in states
  StateMachine[Step, WorkflowContext](states, FetchState(), executeAndMoveStateCrawlPipeline)
}

class CrawlPipelineWorkflow(url: String) extends Workflow {
  val stateMachine : StateMachine[Step, WorkflowContext] = getCrawlPipelineStateMachine(url)
  override def executeAndMoveState(stateInput: WorkflowContext): Step = executeAndMoveStateCrawlPipeline(stateInput)
}