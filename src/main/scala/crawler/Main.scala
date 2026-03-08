package crawler

import crawler.core.Config
import crawler.engine.ExecutionEngine
import crawler.frontier.{DomainFrontier, DomainPriorityQueue}
import crawler.html.CrawlURLState
import crawler.scheduler.Scheduler
import crawler.scheduler.tasks.FrontierCrawlTask

object Main {
  def main(args: Array[String]): Unit = {
    val frontier   = new DomainFrontier()
    val crawlQueue = new DomainPriorityQueue()
    val seenURLs   = new CrawlURLState()
    val engine     = new ExecutionEngine(Config.executionEngineExecutorThreads)

    val task      = new FrontierCrawlTask(engine, frontier, crawlQueue, seenURLs)
    val scheduler = new Scheduler(List(task), engine)

    engine.start()
    scheduler.start()
  }
}
