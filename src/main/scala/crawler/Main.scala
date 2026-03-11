package crawler

import crawler.core.Config
import crawler.engine.ExecutionEngine
import crawler.frontier.{DomainFrontier, DomainPriorityQueue}
import crawler.html.{CrawlURLState, extractRootURL}
import crawler.scheduler.factories.SchedulerFactory

import java.time.LocalDateTime

object Main {
  def main(args: Array[String]): Unit = {
    val frontier   = new DomainFrontier()
    val crawlQueue = new DomainPriorityQueue()
    val seenURLs   = new CrawlURLState()
    val engine     = new ExecutionEngine(Config.executionEngineNormalThreads, Config.executionEngineUrgentThreads)

    val seedUrl = "https://en.wikipedia.org/wiki/Apache_Iceberg"
    val domain  = extractRootURL(seedUrl)
    frontier.addURLToDomain(seedUrl, domain)
    crawlQueue.addDomain(domain, LocalDateTime.MIN)

    val scheduler = SchedulerFactory.createScheduler(frontier, crawlQueue, seenURLs, engine)

    engine.start()
    scheduler.start()
  }
}
