package crawler.engine

import crawler.concurrency.AsyncQueue

class ExecutionThread(taskQueue : AsyncQueue[() => Unit]) {
  private val executor : Executor = Executor(taskQueue)
  @volatile private var running : Boolean = false

  private val thread: Thread = new Thread(() => runLoop())

  def shutDown(): Unit = (running = false)

  def startThread() : Unit = {
    running = true
    thread.start()
  }

  def runLoop() : Unit = {
    while (running) {
      executor.executionFromQueue()
    }
  }
}
