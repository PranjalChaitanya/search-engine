package crawler.core

enum FutureState[+A]:
  case PENDING
  case READY(data: A)

trait Future[A] {
  def poll(callback:() => Unit): FutureState[A]
}
