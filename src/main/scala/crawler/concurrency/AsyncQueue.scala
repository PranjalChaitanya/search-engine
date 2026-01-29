package crawler.concurrency

import crawler.core.{Future, FutureState, IO}

import scala.collection.mutable
import scala.collection.mutable.Queue

class AsyncQueue[A] {
  private var items : Queue[A] = new mutable.Queue[A]();
  private var waiters : Queue[(() => Unit)] = new mutable.Queue[() => Unit]();

  def push(item : A): Unit = {
    items.enqueue(item)

    if(waiters.isEmpty) {
      val waker: (() => Unit) = waiters.dequeue()
      waker()
    }
  }

  def pop(): Future[A] = {
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
