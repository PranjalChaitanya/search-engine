package crawler.engine

import crawler.concurrency.AsyncQueue

class ExecutionEngine(workers: Int) {
  private val executionQueue : AsyncQueue[() => Unit] = AsyncQueue[() => Unit]()
  private val executionPool: List[ExecutionThread] = List.fill(workers)(ExecutionThread(executionQueue))

  def submitJob(jobFunc :() => Unit) : Unit = {
    executionQueue.push(jobFunc)
  }

  def start() : Unit = {
    executionPool.foreach(thread => thread.startThread())
  }

  def shutdown() : Unit = {
    executionPool.foreach(thread => thread.shutDown())
  }
}