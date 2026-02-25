package crawler.engine

import crawler.concurrency.AsyncQueue
import crawler.core.FutureState

import scala.collection.mutable

class Executor(taskQueue : AsyncQueue[() => Unit]) {
  def wake(): Unit = {
    executionFromQueue()
  }
  
  def executionFromQueue(): Unit = {
    taskQueue.pop().poll(wake) match {
      case FutureState.READY(data: (() => Unit)) =>
        data()
      case FutureState.PENDING =>
        return
    }
  }
}
