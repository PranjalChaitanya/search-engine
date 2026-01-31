package crawler.concurrency

import crawler.core.{Future, FutureState, IO}

import scala.collection.mutable
import scala.collection.mutable.Queue

class AsyncQueue[A] {
  private val items : Queue[A] = new mutable.Queue[A]()
  private val waiters : Queue[(() => Unit)] = new mutable.Queue[() => Unit]()
  private val lock = AnyRef

  def push(item : A): Unit = lock.synchronized {
    items.enqueue(item)
    var waker: Option[(() => Unit)] = None

    if(waiters.nonEmpty) {
      waker = Some(waiters.dequeue())
    }

    waker.foreach(_())
  }

  def pop(): Future[A] = lock.synchronized {
    new Future[A]:
      private var done : Boolean = false
      private var registered: Boolean = false
      private var poppedValue : Option[FutureState[A]] = None

      override def poll(callback: () => Unit): FutureState[A] = lock.synchronized {
        poppedValue.getOrElse(
          items.isEmpty match
            case false =>
              done = true
              val readyState: FutureState[A] = FutureState.READY(items.dequeue())
              poppedValue = Some(readyState)
              readyState
            case true =>
              if(!registered) {
                waiters.enqueue(callback)
                registered = true
              }
              FutureState.PENDING
        )
      }
  }
}
