package crawler.workflows

import crawler.core.{StepResult, Workflow, WorkflowContext, WorkflowFailure, WorkflowStep, WorkflowSuccess, WorkflowTransition}
import crawler.engine.ExecutionEngine
import crawler.frontier.{DomainFrontier, DomainPriorityQueue}

import java.time.LocalDateTime

case object FetchAllCrawlableURLStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    val allPoppableDomains : List[String] = DomainPriorityQueue.popAllCrawlableDomains()

    allPoppableDomains.map(domain => {
      DomainPriorityQueue.addDomain(domain, LocalDateTime.now().plusSeconds(10)) // TODO : Change this with a schedueling strategy
    })

    val urls : List[String] = allPoppableDomains.flatMap(domain => {
      DomainFrontier.popURLFromDomain(domain)
    })

    input.ctx.put("url_list", urls)

    WorkflowSuccess
  }
}

case object SubmitNewWorkflowsStep extends WorkflowStep {
  override def run(input: WorkflowContext): StepResult = {
    val executionEngine: ExecutionEngine = input.ctx.get("execution_engine") match {
      case Some(s) => s.asInstanceOf[ExecutionEngine]
      case None =>
        return WorkflowFailure
    }

    val urlList: List[String] = input.ctx.get("url_list") match {
      case Some(s) => s.asInstanceOf[List[String]]
      case None =>
        return WorkflowFailure
    }

    WorkflowSuccess
  }
}

class DispatchCrawlPageWorkflow extends Workflow {
  override val startingStep: WorkflowStep = FetchAllCrawlableURLStep

  override val transitions: Map[WorkflowStep, WorkflowTransition] = Map(
    FetchAllCrawlableURLStep -> WorkflowTransition(
      onSuccess = Some(SubmitNewWorkflowsStep),
      onFailure = None
    ),

    SubmitNewWorkflowsStep -> WorkflowTransition(
      onSuccess = None,
      onFailure = None
    )
  )
}