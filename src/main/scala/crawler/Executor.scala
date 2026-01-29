package crawler

import crawler.concurrency.AsyncQueue
import crawler.core.FutureState

import scala.collection.mutable

class Executor(taskQueue : AsyncQueue[() => Unit]) {
  // TODO : Bug, there's a race condition. What if it calls wait but then you try accessing it and some other 
  // executor has pulled it out when calling pop
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
