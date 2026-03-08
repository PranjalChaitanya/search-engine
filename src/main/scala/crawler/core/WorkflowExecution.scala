package crawler.core

class WorkflowExecution[C](val workflowDefinition: Workflow[C], val context: C) {
  var currentState: Option[WorkflowStep[C]] = Some(workflowDefinition.startingStep)
}

def executeWorkflowStep[C](workflowExecution: WorkflowExecution[C]): Unit = {
  val step: WorkflowStep[C] = workflowExecution.currentState match {
    case Some(s) => s
    case None => return
  }

  val stepResult: StepResult = step.run(workflowExecution.context)
  val transition: WorkflowTransition[C] = workflowExecution.workflowDefinition.transitions.get(step) match {
    case Some(t) => t
    case None =>
      workflowExecution.currentState = None
      return
  }

  stepResult match {
    case WorkflowSuccess => workflowExecution.currentState = transition.onSuccess
    case WorkflowFailure => workflowExecution.currentState = transition.onFailure
  }
}

def executeEntireWorkflow[C](workflowExecution: WorkflowExecution[C]): Unit = {
  while (workflowExecution.currentState.nonEmpty) {
    executeWorkflowStep(workflowExecution)
  }
}
