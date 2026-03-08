package crawler.scheduler.tasks

import crawler.core.executeEntireWorkflow
import crawler.engine.ExecutionEngine
import crawler.frontier.{CrawlQueue, Frontier}
import crawler.html.SeenURLStore
import crawler.scheduler.ScheduledTask
import crawler.workflows.factories.{CrawlPageWorkflowFactory, DispatchCrawlPageWorkflowFactory}

class FrontierCrawlTask(
  val engine: ExecutionEngine,
  val frontier: Frontier,
  val crawlQueue: CrawlQueue,
  val seenURLs: SeenURLStore
) extends ScheduledTask {

  override def getTasks(): List[() => Unit] = {
    val dispatchWorkflowExecution =
      DispatchCrawlPageWorkflowFactory.createDispatchCrawlPageWorkflowExecution(engine, frontier, crawlQueue)

    executeEntireWorkflow(dispatchWorkflowExecution)

    dispatchWorkflowExecution.context.urlList.map(url =>
      CrawlPageWorkflowFactory.createCrawlPageWorkflowExecutionCallback(url, engine, seenURLs)
    )
  }
}
