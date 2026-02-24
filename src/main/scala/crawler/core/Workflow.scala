package crawler.core

import scala.collection.concurrent.TrieMap

trait StepResult

final case class WorkflowStepSuccess(data: Any) extends StepResult
final case class WorkflowStepFailure(data: Any) extends StepResult

class WorkflowContext() {
  val ctx: TrieMap[String, Any] = TrieMap.empty
}

trait WorkflowStep {
  def run(input: WorkflowContext) : StepResult
}

trait WorkflowTransition {
  // Return None when the workflow is completed
  def next(result: StepResult) : Option[WorkflowStep]
}

trait Workflow {
  type StepIdType = String
  
  val startingStep: WorkflowStep
  val workflowSteps: List[(WorkflowStep, WorkflowTransition)]
}