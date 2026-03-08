package crawler.core

sealed trait StepResult
case object WorkflowSuccess extends StepResult
case object WorkflowFailure extends StepResult

case class WorkflowTransition[C](
  onSuccess: Option[WorkflowStep[C]],
  onFailure: Option[WorkflowStep[C]]
)

trait WorkflowStep[C] {
  def run(input: C): StepResult
}

trait Workflow[C] {
  val startingStep: WorkflowStep[C]
  val transitions: Map[WorkflowStep[C], WorkflowTransition[C]]
}
