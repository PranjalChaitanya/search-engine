package crawler.engine

import crawler.concurrency.AsyncQueue

class ExecutionThread(taskQueue : AsyncQueue[() => Unit]) {
  private val executor : Executor = Executor(taskQueue)
  private var running : Boolean = false

  def shutDown(): Unit = (running = false)

  def startThread() : Unit = {
    running = true
    while(running) {
      executor.executionFromQueue()
    }
  }
}
