package crawler.core

trait StepResult

final case class WorkflowStepSuccess(data: Any)
final case class WorkflowStepFailure(data: Any)

class WorkflowContext() {
  val ctx: Map[String, Any] = Map() 
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