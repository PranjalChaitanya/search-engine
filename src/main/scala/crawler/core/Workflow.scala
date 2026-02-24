package crawler.core

import scala.collection.concurrent.TrieMap

sealed trait StepResult
case object WorkflowSuccess extends StepResult
case object WorkflowFailure extends StepResult

case class WorkflowTransition(
  onSuccess : Option[WorkflowStep],
  onFailure : Option[WorkflowStep]
)

class WorkflowContext() {
  val ctx: TrieMap[String, Any] = TrieMap.empty
}

trait WorkflowStep {
  def run(input: WorkflowContext) : StepResult
}

trait Workflow {
  val startingStep: WorkflowStep
  val transitions: Map[WorkflowStep, WorkflowTransition]
}