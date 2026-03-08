package crawler.scheduler

trait ScheduledTask {
  def getTasks() : List[() => Unit]
}
