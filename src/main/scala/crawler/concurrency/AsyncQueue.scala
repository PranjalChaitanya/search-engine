package crawler.concurrency

import crawler.core.{Future, FutureState, IO}

import scala.collection.mutable
import scala.collection.mutable.Queue

class AsyncQueue[A] {
  private var items : Queue[A] = new mutable.Queue[A]()
  private var waiters : Queue[(() => Unit)] = new mutable.Queue[() => Unit]()
  private val lock = AnyRef

  def push(item : A): Unit = lock.synchronized {
    items.enqueue(item)

    if(waiters.isEmpty) {
      val waker: (() => Unit) = waiters.dequeue()
      waker()
    }
  }
  
  // TODO : There is a bug here where if the same thread calls pop multiple times it 
  // will get added to waiter multiple times.
  def pop(): Future[A] = lock.synchronized {
    new Future[A]:
      override def poll(callback: () => Unit): FutureState[A] = {
        items.isEmpty match
          case false =>
            FutureState.READY(items.dequeue())
          case true =>
            waiters.enqueue(callback)
            FutureState.PENDING
      }
  }
}
