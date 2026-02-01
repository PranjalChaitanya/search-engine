package crawler.core

trait WorkflowContext

//case class WorkflowState[A, B](func:A => B) extends State

trait Step {
  type in
  type out
  
  def run(input: in) : out
}

trait WorkflowStep[A, B](func: A => B) extends Step {
  override type in = A
  override type out = B
  
  def run(input: A) : B = {
    func(input)
  }
}

// A, and B correspond to the functions associated with the workflow itself
// C corresponds to the input to the step function
trait Workflow {
  val stateMachine : StateMachine[Step, WorkflowContext]
  def executeAndMoveState(stateInput: WorkflowContext): Step
}