package crawler.scheduler

import crawler.engine.ExecutionEngine

class Scheduler(val scheduledTaskList: List[ScheduledTask], val executionEngine: ExecutionEngine) {
  @volatile private var running = false

  def start(): Unit = {
    running = true
    new Thread(() => loop()).start()
  }

  def stop(): Unit = running = false

  def loop(): Unit = {
    while (running) {
      scheduledTaskList.foreach(scheduledTask => {
        scheduledTask.getTasks().foreach(task => {
          executionEngine.submitJob(task, scheduledTask.priority)
        })
      })
      Thread.sleep(50)
    }
  }
}
