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

//  val workflowTransition = workflowSteps.find((workflowStep, workflowTransition) => {
  val workflowTransition: Option[(WorkflowStep, WorkflowTransition)] = workflowSteps.find((workflowStep, workflowTransition) => {
//    if(workflowStep == workflowExecution.currentState) {
    if(workflowStep.getClass == workflowExecution.currentState.getClass) {
      true
    }
    true
  })

  workflowTransition match {
    case Some((workflowStep, workflowTransition)) =>
      println("A same class is found")
      val executionResult: StepResult = workflowStep.run(workflowExecution.workflowContext)
      workflowExecution.currentState = Some(workflowTransition.next(executionResult)).getOrElse({
        workflowExecution.isComplete = true
        None
      })
    case None =>
      println("A same class is not found")
      return
  }
}