package crawler.core

class WorkflowExecution(workflow: Workflow) {
  // Can optimize this by using a string and looking up a workflow. Bad if you have thousands of copies
  val workflowDefinition: Workflow = workflow
  val workflowContext: WorkflowContext = WorkflowContext()

  var currentState: Option[WorkflowStep] = Some(workflowDefinition.startingStep)
}

def executeWorkflowStep(workflowExecution: WorkflowExecution): Unit = {
  val step: WorkflowStep = workflowExecution.currentState match {
    case Some(s) => s
    case None => return // returned when Step is not defined
  }

  val stepResult : StepResult = step.run(workflowExecution.workflowContext)
  val transition : WorkflowTransition = workflowExecution.workflowDefinition.transitions.get(step) match {
    case Some(s) => s
    case None =>
      workflowExecution.currentState = None
      return
  }
  
  stepResult match {
    case WorkflowSuccess =>
      workflowExecution.currentState = transition.onSuccess
    case WorkflowFailure =>
      workflowExecution.currentState = transition.onFailure
  }
}

def executeEntireWorkflow(workflowExecution: WorkflowExecution) : Unit = {
  println(workflowExecution.currentState)
  val currentState: WorkflowStep = workflowExecution.currentState.getOrElse(return)

  // Calls each step until we eventually reach None
  executeWorkflowStep(workflowExecution)
  executeEntireWorkflow(workflowExecution)
}