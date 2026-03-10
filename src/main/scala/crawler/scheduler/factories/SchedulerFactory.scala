package crawler.scheduler.factories

import crawler.engine.ExecutionEngine
import crawler.frontier.{CrawlQueue, Frontier}
import crawler.html.SeenURLStore
import crawler.scheduler.Scheduler
import crawler.scheduler.tasks.FrontierCrawlTask

object SchedulerFactory {
  def createScheduler(
    frontier: Frontier,
    crawlQueue: CrawlQueue,
    seenURLs: SeenURLStore,
    engine: ExecutionEngine
  ): Scheduler = {
    val task = new FrontierCrawlTask(engine, frontier, crawlQueue, seenURLs)
    new Scheduler(List(task), engine)
  }
}
