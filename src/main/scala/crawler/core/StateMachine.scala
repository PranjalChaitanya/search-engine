package crawler.core

trait State

class StateMachine[A, B] (states: List[A], startingState: A, stepFunction: B => A) {
  var currentState: A = startingState

  def getState: A = currentState
  def setState(newState : A): Unit = currentState = newState

  def step(eventContext: B) : Unit = {
    currentState = stepFunction(eventContext)
  }
}
