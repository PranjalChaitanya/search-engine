package crawler

import crawler.concurrency.AsyncQueue
import crawler.core.FutureState

import scala.collection.mutable

class Executor(taskQueue : AsyncQueue[() => Unit]) {
  def wake(): Unit = {
    executionEventLoop()
  }
  
  def executionEventLoop(): Unit = {
    taskQueue.pop().poll(wake) match {
      case FutureState.READY(data: (() => Unit)) =>
        data()
      case FutureState.PENDING =>
        ()
    }
  }
}
