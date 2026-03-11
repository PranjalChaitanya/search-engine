package crawler.engine

import crawler.concurrency.AsyncQueue

class ExecutionEngine(normalWorkers: Int, urgentWorkers: Int = 2) {
  require(urgentWorkers > 0 && normalWorkers > 0)

  private val normalQueue: AsyncQueue[() => Unit] = AsyncQueue[() => Unit]()
  private val urgentQueue: AsyncQueue[() => Unit] = AsyncQueue[() => Unit]()

  private val normalPool: List[ExecutionThread] =
    List.fill(normalWorkers)(ExecutionThread(normalQueue))
  private val urgentPool: List[ExecutionThread] =
    List.fill(urgentWorkers)(ExecutionThread(urgentQueue))

  def submitJob(jobFunc: () => Unit, priority: Priority = Priority.NORMAL): Unit =
    priority match {
      case Priority.HIGH   => urgentQueue.push(jobFunc)
      case Priority.NORMAL => normalQueue.push(jobFunc)
    }

  def start(): Unit = {
    normalPool.foreach(_.startThread())
    urgentPool.foreach(_.startThread())
  }

  def shutdown(): Unit = {
    normalPool.foreach(_.shutDown())
    urgentPool.foreach(_.shutDown())
  }
}
