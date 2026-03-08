package crawler.scheduler

import crawler.engine.ExecutionEngine

class Scheduler(val scheduledTaskList: List[ScheduledTask], val executionEngine: ExecutionEngine) {
  def start(): Unit = new Thread(() => loop()).start()

  def loop(): Unit = {
    while (true) {
      scheduledTaskList.foreach(scheduledTask => {
        scheduledTask.getTasks().foreach(task => {
          executionEngine.submitJob(task)
        })
      })
      Thread.sleep(1000)
    }
  }
}