package crawler.scheduler

import crawler.engine.Priority

trait ScheduledTask {
  def priority: Priority = Priority.NORMAL
  def getTasks(): List[() => Unit]
}
