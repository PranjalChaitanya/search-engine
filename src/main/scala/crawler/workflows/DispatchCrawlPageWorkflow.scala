package crawler.workflows

import crawler.core.{StepResult, Workflow, WorkflowFailure, WorkflowStep, WorkflowSuccess, WorkflowTransition}
import crawler.engine.ExecutionEngine
import crawler.frontier.{DomainFrontier, DomainPriorityQueue}

import java.time.LocalDateTime

class DispatchContext(val executionEngine: ExecutionEngine) {
  var urlList: List[String] = List.empty
}

case object FetchAllCrawlableURLStep extends WorkflowStep[DispatchContext] {
  override def run(input: DispatchContext): StepResult = {
    val allPoppableDomains: List[String] = DomainPriorityQueue.popAllCrawlableDomains()

    allPoppableDomains.foreach(domain =>
      DomainPriorityQueue.addDomain(domain, LocalDateTime.now().plusSeconds(10))
    )

    input.urlList = allPoppableDomains.flatMap(domain => DomainFrontier.popURLFromDomain(domain))

    WorkflowSuccess
  }
}

case object SubmitNewWorkflowsStep extends WorkflowStep[DispatchContext] {
  override def run(input: DispatchContext): StepResult = {
    WorkflowSuccess
  }
}

class DispatchCrawlPageWorkflow extends Workflow[DispatchContext] {
  override val startingStep: WorkflowStep[DispatchContext] = FetchAllCrawlableURLStep

  override val transitions: Map[WorkflowStep[DispatchContext], WorkflowTransition[DispatchContext]] = Map(
    FetchAllCrawlableURLStep -> WorkflowTransition(Some(SubmitNewWorkflowsStep), None),
    SubmitNewWorkflowsStep   -> WorkflowTransition(None, None)
  )
}
