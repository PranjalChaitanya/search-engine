package crawler.core

class WorkflowExecution(workflow: Workflow) {
  // Can optimize this by using a string and looking up a workflow. Bad if you have thousands of copies
  val workflowDefinition: Workflow = workflow
  val workflowContext: WorkflowContext = WorkflowContext()

  var currentState: Option[WorkflowStep] = Some(workflowDefinition.startingStep)
  var isComplete: Boolean = false
}

def executeWorkflowStep(workflowExecution: WorkflowExecution): Unit = {
  val workflowSteps = workflowExecution.workflowDefinition.workflowSteps

  val workflowTransition: Option[(WorkflowStep, WorkflowTransition)] = workflowSteps.find((workflowStep, workflowTransition) => {
    if(workflowStep == workflowExecution.currentState) {
      return true
    }
    return false
  })

  workflowTransition match {
    case Some((workflowStep, workflowTransition)) =>
      val executionResult: StepResult = workflowStep.run(workflowExecution.workflowContext)
      workflowExecution.currentState = Some(workflowTransition.next(executionResult)).getOrElse({
        workflowExecution.isComplete = true
        None
      })
    case None =>
      return
  }
}