package crawler.core

trait Workflow[A, B] {
  val stateMachine : StateMachine[A, B]
}
